package com.assignment_alert.Assignment_Alert.assignments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import com.assignment_alert.Assignment_Alert.courses.Course;
import com.assignment_alert.Assignment_Alert.courses.CourseRepository;
import com.assignment_alert.Assignment_Alert.user.User;
import com.assignment_alert.Assignment_Alert.user.UserRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sync_isolation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AssignmentSyncIsolationTest {

    private static final Long SHARED_CANVAS_COURSE_ID = 177530L;
    private static final Long SHARED_CANVAS_ASSIGNMENT_ID = 2757333L;
    private static final String SHARED_COURSE_URL = "https://canvas.odu.edu/courses/177530";

    @Autowired
    private AssignmentRepository assignmentRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EntityManager entityManager;

    private User alice;
    private User bob;
    private Course aliceCourse;
    private Course bobCourse;
    private Assignment aliceAssignment;
    private Assignment bobAssignment;

    @BeforeEach
    void setUp() {
        alice = saveUser(1001L, "alice@school.edu");
        bob = saveUser(1002L, "bob@school.edu");

        aliceCourse = saveCourse(alice);
        bobCourse = saveCourse(bob);

        aliceAssignment = saveAssignment(aliceCourse, "Project 1 Milestone", Priority.HIGH);
        bobAssignment = saveAssignment(bobCourse, "Project 1 Milestone", Priority.HIGH);

        flushAndClear();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private User saveUser(Long canvasId, String email) {
        User user = new User();
        user.setCanvasId(canvasId);
        user.setName("Student " + canvasId);
        user.setEmail(email);
        user.setSchoolDomain("https://canvas.odu.edu");
        return userRepo.save(user);
    }

    private Course saveCourse(User owner) {
        Course course = new Course();
        course.setCanvasCourseId(SHARED_CANVAS_COURSE_ID);
        course.setCourseName("DATA STRUCTURES & ALGORITHMS");
        course.setUrl(SHARED_COURSE_URL);
        course.setUser(owner);
        return courseRepo.save(course);
    }

    private Assignment saveAssignment(Course course, String name, Priority priority) {
        return assignmentRepo.save(newAssignment(course, name, priority));
    }

    private Assignment newAssignment(Course course, String name, Priority priority) {
        Assignment assignment = new Assignment();
        assignment.setAssignmentName(name);
        assignment.setCanvasAssignmentId(SHARED_CANVAS_ASSIGNMENT_ID);
        assignment.setCanvasCourseId(SHARED_CANVAS_COURSE_ID);
        assignment.setCourse(course);
        assignment.setDueAt(LocalDateTime.of(2026, 10, 1, 23, 59));
        assignment.setCreatedDate(LocalDateTime.now());
        assignment.setCompleted(false);
        assignment.setPriority(priority);
        assignment.setUrl(SHARED_COURSE_URL + "/assignments/" + SHARED_CANVAS_ASSIGNMENT_ID);
        assignment.setReminderSent(false);
        assignment.setBlockingEnabled(false);
        assignment.setLastSynced(LocalDateTime.now());
        assignment.setSubmissionType("online_upload");
        return assignment;
    }

    @Test
    void twoUsersCanHoldTheSameCourse() {
        assertThat(courseRepo.findByCanvasCourseIdAndUser(SHARED_CANVAS_COURSE_ID, alice)).isPresent();
        assertThat(courseRepo.findByCanvasCourseIdAndUser(SHARED_CANVAS_COURSE_ID, bob)).isPresent();
        assertThat(aliceCourse.getCourseId()).isNotEqualTo(bobCourse.getCourseId());
    }

    @Test
    void twoUsersCanHoldTheSameCanvasAssignment() {
        assertThat(assignmentRepo.findAll()).hasSize(2);
        assertThat(aliceAssignment.getAssignmentId()).isNotEqualTo(bobAssignment.getAssignmentId());
    }

    @Test
    void theSyncLookupOnlyFindsTheCopyForTheCourseBeingSynced() {
        Course reloadedAliceCourse = courseRepo.findById(aliceCourse.getCourseId()).orElseThrow();

        Optional<Assignment> found = assignmentRepo
                .findByCanvasAssignmentIdAndCourse(SHARED_CANVAS_ASSIGNMENT_ID, reloadedAliceCourse);

        assertThat(found).isPresent();
        assertThat(found.get().getAssignmentId()).isEqualTo(aliceAssignment.getAssignmentId());
        assertThat(found.get().getCourse().getUser().getUserId()).isEqualTo(alice.getUserId());
    }

    @Test
    void syncingOneUsersCourseLeavesTheOtherUsersCopyUntouched() {
        Course reloadedAliceCourse = courseRepo.findById(aliceCourse.getCourseId()).orElseThrow();

        Assignment target = assignmentRepo
                .findByCanvasAssignmentIdAndCourse(SHARED_CANVAS_ASSIGNMENT_ID, reloadedAliceCourse)
                .orElseThrow();
        target.setAssignmentName("Project 1 Milestone (updated by sync)");
        target.setDueAt(LocalDateTime.of(2026, 11, 15, 12, 0));
        target.setCompleted(true);
        target.setPriority(Priority.LOW);
        assignmentRepo.save(target);

        flushAndClear();

        Assignment untouched = assignmentRepo.findById(bobAssignment.getAssignmentId()).orElseThrow();
        assertThat(untouched.getAssignmentName()).isEqualTo("Project 1 Milestone");
        assertThat(untouched.getDueAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 23, 59));
        assertThat(untouched.getCompleted()).isFalse();
        assertThat(untouched.getPriority()).isEqualTo(Priority.HIGH);

        Assignment updated = assignmentRepo.findById(aliceAssignment.getAssignmentId()).orElseThrow();
        assertThat(updated.getAssignmentName()).isEqualTo("Project 1 Milestone (updated by sync)");
    }

    @Test
    void oneUserCannotReadAnotherUsersAssignmentById() {
        assertThat(assignmentRepo.findByAssignmentIdAndCourse_User(bobAssignment.getAssignmentId(), alice))
                .isEmpty();
        assertThat(assignmentRepo.findByAssignmentIdAndCourse_User(bobAssignment.getAssignmentId(), bob))
                .isPresent();
    }

    @Test
    void userScopedCanvasLookupOnlyReturnsThatUsersCopy() {
        Optional<Assignment> forAlice = assignmentRepo
                .findByCanvasAssignmentIdAndCourse_User(SHARED_CANVAS_ASSIGNMENT_ID, alice);

        assertThat(forAlice).isPresent();
        assertThat(forAlice.get().getAssignmentId()).isEqualTo(aliceAssignment.getAssignmentId());
    }

    @Test
    void listingUpcomingAssignmentsNeverCrossesUsers() {
        LocalDateTime cutoff = LocalDateTime.of(2026, 9, 1, 0, 0);

        assertThat(assignmentRepo.findByDueAtAfterAndCourse_UserOrderByDueAtAsc(cutoff, alice))
                .extracting(Assignment::getAssignmentId)
                .containsExactly(aliceAssignment.getAssignmentId());
    }

    @Test
    void theSameCanvasAssignmentCannotBeStoredTwiceInOneCourse() {
        Course reloadedAliceCourse = courseRepo.findById(aliceCourse.getCourseId()).orElseThrow();
        Assignment duplicate = newAssignment(reloadedAliceCourse, "Duplicate", Priority.LOW);

        Throwable thrown = catchThrowable(() -> assignmentRepo.saveAndFlush(duplicate));

        assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(thrown.getMessage().toLowerCase(Locale.ROOT))
                .contains("unique_canvas_assignment_per_course");
    }
}
