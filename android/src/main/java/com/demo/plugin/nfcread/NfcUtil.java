package com.demo.plugin.nfcread;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TODO:功能说明 Nfc工具类
 *
 * @author: chenqiuyang
 * @date: 2018-07-12 11:33
 */
public class NfcUtil {
    private static final String TAG = "NfcUtil";

    /**
     * 解析 ndefRecord 文本数据
     * @param ndefRecord
     * @return
     */
    public static String parse(NdefRecord ndefRecord) {
        // verify tnf   得到TNF的值
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }

        // 得到字节数组进行判断
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }

        try {
            // 获得一个字节流
            byte[] payload = ndefRecord.getPayload();
            // payload[0]取第一个字节。 0x80：十六进制（最高位是1剩下全是0）
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8"
                    : "UTF-16";
            // 获得语言编码长度
            int languageCodeLength = payload[0] & 0x3f;
            // 获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");
            //
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);

            return text;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }


    //初次判断是什么类型的NFC卡
    public static NdefMessage[] getNdefMsg(Intent intent) {
        if (intent == null)
            return null;

        //nfc卡支持的格式
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] temp = tag.getTechList();
        for (String s : temp) {
            Log.i(TAG, "resolveIntent tag: " + s);
        }


        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] ndefMessages;

            // 判断是哪种类型的数据 默认为NDEF格式
            if (rawMessage != null) {
                Log.i(TAG, "getNdefMsg: ndef格式 ");
                ndefMessages = new NdefMessage[rawMessage.length];
                for (int i = 0; i < rawMessage.length; i++) {
                    ndefMessages[i] = (NdefMessage) rawMessage[i];
                }
            } else {
                //未知类型
                Log.i(TAG, "getNdefMsg: 未知类型");
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable parcelable = intent
                        .getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(parcelable).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                ndefMessages = new NdefMessage[]{msg};
            }


            return ndefMessages;
        }

        return null;
    }



    //一般公家卡，扫描的信息
    private static String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("Tag ID (16进制)： ").append(byte2hex(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");




        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        return sb.toString();
    }



    private static String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b
     *            byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toLowerCase();
    }
    private static long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            System.out.println("测试====="+textRecord);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    @SuppressLint("MissingPermission")
    static void writeNdef(Intent intent){
        if (intent == null)
            return ;

        //nfc卡支持的格式
        Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mTag==null){
            System.out.println("不能识别的标签类型！");
            return;
        }
        Ndef ndef= Ndef.get(mTag);//获取ndef对象
        if (!ndef.isWritable()){
            System.out.println("该标签不能写入数据!");
            return;
        }
        try {
        ndef.connect();
        NdefRecord ndefRecord=createTextRecord("123456");//创建一个NdefRecord对象
        NdefMessage ndefMessage=new NdefMessage(new NdefRecord[]{ndefRecord});//根据NdefRecord数组，创建一个NdefMessage对象
        int size=ndefMessage.getByteArrayLength();
        if (ndef.getMaxSize()<size){
            System.out.println("标签容量不足！");
            return;
        }
        NdefFormatable ndefFormatable = NdefFormatable.get(mTag);


            if (ndef != null) {
//                ndef.connect();
                ndef.writeNdefMessage(ndefMessage);
                System.out.println("NDEF数据写入成功！===");
                return ;
            } else if(ndefFormatable != null) {
                ndefFormatable.connect();
                ndefFormatable.format(ndefMessage);
                System.out.println("数据写入成功！===");
                return ;
            }

//            ndef.connect();//连接
//            ndef.writeNdefMessage(ndefMessage);//写数据
//            System.out.println("数据写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }finally {
            try {
                ndef.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;

    }

    /**
     * 写数据
     * @param ndefMessage 创建好的NDEF文本数据
     * @param tag 标签
     * @return
     */
    public static boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(ndefMessage);
            return true;
        } catch (Exception e) {
        }
        return false;
    }



    /** Parse an NdefMessage */
    public static List<ParsedNdefRecord> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static List<ParsedNdefRecord> getRecords(NdefRecord[] records) {
        List<ParsedNdefRecord> elements = new ArrayList<ParsedNdefRecord>();
//        for (final NdefRecord record : records) {
//            if (UriRecord.isUri(record)) {
//                elements.add(UriRecord.parse(record));
//            } else if (TextRecord.isText(record)) {
//                elements.add(TextRecord.parse(record));
//            } else if (SmartPoster.isPoster(record)) {
//                elements.add(SmartPoster.parse(record));
//            } else {
////            	String temp ="";
////            	temp = new String(record.getPayload());
////            	temp = bytesToHexString(record.getPayload(),true);
//                elements.add(new ParsedNdefRecord() {
//                    @Override
//                    public String getViewText() {
//                        String temp = new String(record.getPayload()) + "\n";
//                        return temp;
//                    }
//                });
//            }
//        }
        return elements;
    }
    }
