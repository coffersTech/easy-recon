const RealtimeReconService = require('../../sdk/node.js/service/RealtimeReconService');

// Mock ReconRepository
const mockReconRepository = {
    saveOrderMain: async (orderMain) => {
        console.log('Saving Order Main:', orderMain);
        return true; // Simulate success
    },
    batchSaveOrderSplitSub: async (splitSubs) => {
        console.log('Batch Saving Order Split Subs:', splitSubs);
        return true; // Simulate success
    },
    updateReconStatus: async (orderNo, status) => {
        console.log(`Updating Recon Status for ${orderNo} to ${status}`);
        return true; // Simulate success
    }
};

// Mock AlarmService
const mockAlarmService = {
    sendAlarm: (message) => {
        console.log('Sending Alarm:', message);
    }
};

// Instantiate Service
const reconService = new RealtimeReconService(mockReconRepository, mockAlarmService);

// Create Mock Data
const orderMain = {
    orderNo: 'ORD-1234567890',
    amount: 100.00,
    merchantId: 'MCH-001',
    transactionTime: new Date()
};

const splitSubs = [
    { subOrderNo: 'SUB-001', amount: 80.00, type: 'settlement' },
    { subOrderNo: 'SUB-002', amount: 20.00, type: 'fee' }
];

// Run Demo
async function runDemo() {
    console.log('--- Starting Node.js Easy Recon SDK Demo ---');
    try {
        const result = await reconService.doRealtimeRecon(orderMain, splitSubs);
        if (result) {
            console.log('--- Recon Successful ---');
        } else {
            console.log('--- Recon Failed ---');
        }
    } catch (error) {
        console.error('Error executing demo:', error);
    }
}

runDemo();
