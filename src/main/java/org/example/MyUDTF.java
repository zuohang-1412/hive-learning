package org.example;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

// 一进多出
public class MyUDTF extends GenericUDTF {
    // explode
    private List<String> output = new ArrayList<String>();

    @Override
    public StructObjectInspector initialize(StructObjectInspector argOIs) throws UDFArgumentException {
        List<String> structFieldNames = new ArrayList<String>();
        // 输出数据列名为word
        structFieldNames.add("word");
        // 输出数据格式
        List<ObjectInspector> structFieldType = new ArrayList<ObjectInspector>();
        structFieldType.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        // 返回格式和列名
        return ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldType);
    }

    @Override
    public void process(Object[] objects) throws HiveException {
        // objects 格式为 ("1,2,3", ",")
        String input = objects[0].toString();
        String s = objects[1].toString();
        String[] data = input.split(s);
        for (String da : data) {
            output.clear();
            output.add(da);
            forward(output);
        }

    }

    @Override
    public void close() throws HiveException {

    }
}
