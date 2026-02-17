/**
 * Easy Recon SDK Node.js Demo
 * 包含 11 个演示场景
 */
const {
    EasyReconApi,
    ReconOrderMain,
    ReconOrderSplitSub,
    ReconOrderRefundSplitSub,
    ReconNotifyLog
} = require('@cofferstech/easy-recon-sdk');
const { Client } = require('pg');

// 配置
const config = {
    enabled: true,
    amountTolerance: '0.01',
    alarm: {
        type: 'log' // Use 'dingtalk' with webhookUrl for real alarms
    },
    // Database connection config for postgresql
    database: {
        host: 'localhost',
        user: 'postgresql', // Matches Java Demo
        password: 'postgresql', // Matches Java Demo
        database: 'easy_recon_demo',
        port: 5432,
    }
};

// Create Connection
async function createConnection() {
    const client = new Client(config.database);
    await client.connect();
    return client;
}

// Main Demo Runner
async function runDemo() {
    console.log('=== Easy Recon SDK Node.js Demos Start (PostgreSQL) ===');

    let connection;
    try {
        connection = await createConnection();
        const sdk = new EasyReconApi(config, connection);

        // Initialize Tables (Optional, creating tables if not exist)
        const ddl = sdk.getCreateTableSQL();
        for (const sql of ddl) {
            await connection.query(sql);
        }

        // [场景 1] 同步实时对账
        await scenario1_SyncRecon(sdk);

        // [场景 2] 退款对账
        await scenario2_RefundRecon(sdk);

        // [场景 3] 异步对账
        await scenario3_AsyncRecon(sdk);

        // [场景 4] 异常处理
        await scenario4_ExceptionHandling(sdk);

        // [场景 5] 定时核账
        await scenario5_TimingRecon(sdk);

        // [场景 6] 报表统计
        await scenario6_ReportStats(sdk);

        // [场景 7] 通知状态演变
        await scenario7_NotifyEvolution(sdk);

        // [场景 8] 回调接口演练
        await scenario8_CallbackInterface(sdk);

        // [场景 9] 多商户闭环
        await scenario9_MultiMerchantClosure(sdk);

        // [场景 10] 子订单退款
        await scenario10_SubOrderRefund(sdk);

        // [场景 11] 原始单号对账
        await scenario11_MerchantNoRecon(sdk);

    } catch (e) {
        console.error('Demo Error:', e);
    } finally {
        if (connection) await connection.end();
        console.log('=== Easy Recon SDK Node.js Demos End ===');
    }
}

// Helper to generate IDs
function genId(prefix) { return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`; }

// --- Scenarios ---

async function scenario1_SyncRecon(sdk) {
    console.log('\n--- [场景 1] 同步实时对账 ---');
    const orderNo = genId('ORD-NODE-1');

    // 1. Order Main
    const main = new ReconOrderMain();
    main.orderNo = orderNo;
    main.merchantId = 'MCH001';
    main.merchantName = 'Merchant One';
    main.orderAmount = '100.00';
    main.payAmount = '100.00'; // Match
    main.payStatus = 1; // Success
    main.orderTime = new Date();
    main.payTime = new Date();

    // 2. Split Subs
    const sub1 = new ReconOrderSplitSub();
    sub1.orderNo = orderNo;
    sub1.subOrderNo = genId('SUB-1');
    sub1.merchantId = 'SUB_MCH_A';
    sub1.splitAmount = '80.00';
    sub1.status = 1;

    const sub2 = new ReconOrderSplitSub();
    sub2.orderNo = orderNo;
    sub2.subOrderNo = genId('SUB-2');
    sub2.merchantId = 'SUB_MCH_B';
    sub2.splitAmount = '20.00';
    sub2.status = 1;

    const result = await sdk.reconOrder(main, [sub1, sub2]);
    console.log(`同步实时对账结果: ${result ? '成功' : '失败'}`);
}

async function scenario2_RefundRecon(sdk) {
    console.log('\n--- [场景 2] 退款对账 ---');
    // Using order from Scenario 1 for refund? Or new one? Let's use new one for clarity or reuse previous orderNo logic if tracking.
    // For demo simplicity, create new order then refund it immediately.
    const orderNo = genId('ORD-NODE-2');
    const main = new ReconOrderMain();
    main.orderNo = orderNo;
    main.merchantId = 'MCH002';
    main.orderAmount = '100.00';
    main.payAmount = '100.00';
    main.payStatus = 1;
    main.orderTime = new Date();
    await sdk.reconOrder(main, []);

    // Refund
    const refundAmount = '30.00';
    const subRefund = new ReconOrderRefundSplitSub();
    subRefund.orderNo = orderNo;
    subRefund.subOrderNo = genId('SUB-REF-1');
    subRefund.merchantId = 'SUB_MCH_A';
    subRefund.refundSplitAmount = '30.00';
    subRefund.status = 1;

    const result = await sdk.reconRefund(orderNo, refundAmount, [subRefund]);
    console.log(`退款对账结果: ${result ? '成功' : '失败'}`);
}

async function scenario3_AsyncRecon(sdk) {
    console.log('\n--- [场景 3] 异步对账 ---');
    const orderNo = genId('ORD-NODE-3');
    const main = new ReconOrderMain();
    main.orderNo = orderNo;
    main.merchantId = 'MCH003';
    main.orderAmount = '200.00';
    main.payAmount = '200.00';
    main.orderTime = new Date();

    // Node.js is naturally async, simulated by not awaiting immediately or using Promise.all
    console.log('发起异步对账请求...');
    sdk.reconOrder(main, []).then(res => {
        console.log(`异步对账回调结果: ${res ? '成功' : '失败'}`);
    });
    // Wait for it briefly
    await new Promise(r => setTimeout(r, 100));
}

async function scenario4_ExceptionHandling(sdk) {
    console.log('\n--- [场景 4] 异常处理 (模拟金额不一致) ---');
    const orderNo = genId('ORD-NODE-4');
    const main = new ReconOrderMain();
    main.orderNo = orderNo;
    main.merchantId = 'MCH004';
    main.orderAmount = '100.00';
    main.payAmount = '99.00'; // Mismatch
    main.orderTime = new Date();

    const result = await sdk.reconOrder(main, []);
    // Note: Our SDK logic saves exception and sets status=FAIL (2), returns true (meaning processed)
    // Or returns false? Let's check logic: returns true but status is 2.
    // Actually our code returns true and logs exception.
    console.log(`异常对账处理完成 (查看数据库异常表): ${result}`);
}

async function scenario5_TimingRecon(sdk) {
    console.log('\n--- [场景 5] 定时核账 ---');
    const dateStr = new Date().toISOString().split('T')[0];
    const result = await sdk.runTimingRecon(dateStr);
    console.log(`定时核账结果: ${result ? '成功' : '失败'}`);
}

async function scenario6_ReportStats(sdk) {
    // Requires summary logic in SDK (not fully implemented in repo yet, logic just logs)
    console.log('\n--- [场景 6] 报表统计 ---');
    console.log('功能暂未完全实现，需查看数据库 summary 表');
}

async function scenario7_NotifyEvolution(sdk) {
    console.log('\n--- [场景 7] 通知状态演变 ---');
    const orderNo = genId('ORD-NODE-7');
    const main = new ReconOrderMain();
    main.orderNo = orderNo;
    main.merchantId = 'MCH007';
    main.orderAmount = '50.00';
    main.payAmount = '50.00';
    main.notifyStatus = 0; // Init
    await sdk.reconOrder(main, []);

    // Simulate Notify
    const notifyLog = new ReconNotifyLog();
    notifyLog.orderNo = orderNo;
    notifyLog.notifyStatus = 1; // Success
    notifyLog.notifyResult = 'OK';

    await sdk.reconNotify(notifyLog);
    console.log('通知处理完成，订单状态已更新');
}

async function scenario8_CallbackInterface(sdk) {
    console.log('\n--- [场景 8] 回调接口演练 ---');
    // Similar to Scenario 7 but using specific interface
    // Node SDK reconNotify is the interface
    const orderNo = genId('ORD-NODE-8');
    const log = new ReconNotifyLog();
    log.orderNo = orderNo;
    log.notifyStatus = 2; // Fail
    await sdk.reconNotify(log);
    console.log('回调处理完成');
}

async function scenario9_MultiMerchantClosure(sdk) {
    console.log('\n--- [场景 9] 多商户闭环 ---');
    // Logic similar to Sync but checking closure
    console.log('逻辑同场景1，略');
}

async function scenario10_SubOrderRefund(sdk) {
    console.log('\n--- [场景 10] 子订单退款 ---');
    // Logic supported by reconRefund
    console.log('逻辑同场景2，略');
}

async function scenario11_MerchantNoRecon(sdk) {
    console.log('\n--- [场景 11] 原始单号对账 ---');
    // Needs support in SDK methods lookups. 
    // Current SDK methods are orderNo based mainly.
    // To support merchantOrderNo, we'd need getOrderMainByMerchantOrderNo.
    // Not fully implementing in this pass.
    console.log('SDK 暂未完全支持原始单号查询 (需增加索引及接口)');
}

runDemo().catch(console.error);
