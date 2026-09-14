// Background script for Assignment Alert Chrome extension

// Listen for alarms
chrome.alarms.onAlarm.addListener((alarm) => {
    console.log('Alarm triggered:', alarm.name);
    // Handle alarm logic here
});

// Example: Create an alarm (you can call this from popup or content script)
function createAlarm(name: string, delayInMinutes: number) {
    chrome.alarms.create(name, { delayInMinutes });
}

// Export for use in other scripts if needed
export { createAlarm };