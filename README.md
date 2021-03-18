

##可能是最简单的ble框架，功能如下
1.搜索列表
2.连接ble
3.收发数据
4.蓝牙开关控制
5.蓝牙状态广播



##快速使用
1.初始化蓝牙
    private void intiBle() {
        if (BleManger.INATAN.isSupperBle(this)) {
            //初始化蓝牙
            BleManger.INATAN.init(this);
            //添加特征值
            BleCharacteristic characteristic = new BleCharacteristic();
            characteristic.setServiceUUID(BleUUUID.serviceUUUID);
            characteristic.setNotifyUUUID(BleUUUID.notifyUUUID);
            characteristic.setWriteUUUID(BleUUUID.writeUUUID);
            BleManger.INATAN.addBleCharacteristic(characteristic);
            //设置后台扫描蓝牙
            BleManger.INATAN.setScanBackstage(true);
        } else {
            Logger.d("=========手机不支持蓝牙");
        }


2.添加蓝牙回调
        bleConnectCallBack = new BleConnectCallBack("main") {
            @Override
            public void connectSuccess() {
                super.connectSuccess();
                mainConnectState.setText("连接");
            }

            @Override
            public void connectFail(String errorMsg) {
                mainConnectState.setText(errorMsg);
            }

            @Override
            public void writeTimeOut() {
                addLog("发送超时");
            }

            @Override
            public void handleMsg(String hexString, byte[] bytes) {
                addLog(hexString);
            }

            @Override
            public void sendFail(String errorCode) {
                addLog(errorCode);
            }
        };
        BleManger.INATAN.addBleConnectCallBack(bleConnectCallBack);

3.发送数据
   byte[] data = HexUtils.hexStr2Bytes(str);
   BleManger.INATAN.sendData(data);