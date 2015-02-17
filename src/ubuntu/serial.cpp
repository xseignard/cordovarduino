/*
 * Copyright 2015 Loci Controls Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Author: Ian Martin <ian@locicontrols.com>
 */

#include <cstdio>
#include <QtCore>

#include "serial.h"

#define error_desc(desc) "'" desc "'"

static QString byteArrayToJavaScript(QByteArray data) {
    int n;
    char buf[5];
    QString qs = "\"";

    for (n = 0; n < data.size(); n++) {
        sprintf(buf, "\\x%02x", data[n] & 0xff);
        qs += buf;
    }

    return qs + "\"";
}

void Serial::onDataAvailable(void) {
    if (port == NULL) return;
    QByteArray data = port->readAll();
    if (_scId != 0) callbackWithoutRemove(_scId, byteArrayToJavaScript(data));
}

Serial::Serial(Cordova *cordova) : CPlugin(cordova) {
    _scId = 0;
    _ecId = 0;
}

void Serial::requestPermission(int scId, int ecId, const QVariantMap& arg) {
    callback(scId, "{}");
}

void Serial::openSerial(int scId, int ecId, const QVariantMap& arg) {
    bool result;

    QVariantMap opts = arg["opts"].toMap();

    port = new QSerialPort(opts.value("device", "/dev/ttyUSB0").toString());
    if (port == NULL) {
        callback(ecId, error_desc("QSerialPort constructor failed"));
        return;
    }

    port->setBaudRate((QSerialPort::BaudRate)opts.value("baudRate", 9600).toInt());
    port->setParity(opts.contains("parity")? (QSerialPort::Parity)opts["parity"].toInt() : QSerialPort::NoParity);
    port->setFlowControl(QSerialPort::NoFlowControl);
    port->setDataBits((QSerialPort::DataBits)opts.value("dataBits", 8).toInt());
    port->setStopBits(opts.contains("stopBits")? (QSerialPort::StopBits)opts["stopBits"].toInt() : QSerialPort::OneStop);

    readWaitMillis = opts.value("readWaitMillis", 200).toInt();

    if (!connect(port, SIGNAL(readyRead()), this, SLOT(onDataAvailable()))) {
        callback(ecId, error_desc("connect() failed"));
        return;
    }

    if (!port->open(QIODevice::ReadWrite)) {
        callback(ecId, error_desc("QSerialPort::open() failed"));
        return;
    }

    callback(scId, "{}");
}

void Serial::writeSerial(int scId, int ecId, const QVariantMap& arg) {
    if (port->isOpen()) {
        QByteArray data = arg["data"].toString().toLatin1();

        qint64 written = 0;
        while (written < data.size()) {
            qint64 result = port->write(data.mid(written));
            if (result < 0) {
                callback(ecId, error_desc("Error writing data"));
                return;
            } else {
                written += result;
            }
        }

        callback(scId, "{}");
    } else {
        callback(ecId, error_desc("Port not open"));
    }
}

static QByteArray hex2bin(QString hexString) {
    QByteArray result = "";

    while(hexString.size() >= 2) {
        result.append(QChar(hexString.left(2).toInt(NULL, 16)));
        hexString.remove(0, 2);
    }

    return result;
}

void Serial::writeSerialHex(int scId, int ecId, const QVariantMap& arg) {
    QVariantMap obj = arg;
    obj["data"] = hex2bin(arg["data"].toString());
    writeSerial(scId, ecId, obj);
}

void Serial::readSerial(int scId, int ecId) {
    if (port->isOpen()) {
        if ( (port->bytesAvailable() > 0) || port->waitForReadyRead(readWaitMillis) ) {
            callback(scId, byteArrayToJavaScript(port->readAll()));
        } else {
            callback(ecId, error_desc("No data available"));
        }
    } else {
        callback(ecId, error_desc("Port not open"));
    }
}

void Serial::closeSerial(int scId, int ecId) {
    if (port->isOpen()) {
        callback(scId, "{}");
    } else {
        callback(ecId, error_desc("Port not open"));
    }
}

void Serial::registerReadCallback(int scId, int ecId) {
    _scId = scId;
    _ecId = ecId;
}
