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

#ifndef SERIAL_H
#define SERIAL_H

#include <QSerialPort>
#include <cplugin.h>

class Serial: public CPlugin {
    Q_OBJECT
public:
    explicit Serial(Cordova *cordova);

    virtual const QString fullName() override {
        return Serial::fullID();
    }

    virtual const QString shortName() override {
        return "Serial";
    }

    static const QString fullID() {
        return "fr.drangies.cordova.serial";
    }

public slots:
    void requestPermission(int scId, int ecId, const QVariantMap& arg);
    void openSerial(int scId, int ecId, const QVariantMap& arg);
    void writeSerial(int scId, int ecId, const QVariantMap& arg);
    void writeSerialHex(int scId, int ecId, const QVariantMap& arg);
    void readSerial(int scId, int ecId);
    void closeSerial(int scId, int ecId);
    void registerReadCallback(int scId, int ecId);

private slots:
    void onDataAvailable(void);

private:
    QSerialPort* port;

    int readWaitMillis;

    int _scId;
    int _ecId;
};

#endif
