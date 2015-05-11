package cn.rfidcn.anticheat.scheme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.alibaba.fastjson.JSON;

public class AvroScheme implements Scheme{

	@Override
	public List<Object> deserialize(byte[] bytes) {
		 List data = new ArrayList();
		 SeekableByteArrayInput seekable = new SeekableByteArrayInput(bytes);
		 DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>();
		 DataFileReader<GenericRecord> dataFileReader = null;
		 Class clazz = null;
	     try {
			 dataFileReader = new DataFileReader<GenericRecord>(seekable, datumReader);
			 clazz = Class.forName("cn.rfidcn.anticheat.model."+dataFileReader.getSchema().getName());
			 GenericRecord activity = null;
			 while (dataFileReader.hasNext()) {
		            activity = dataFileReader.next(activity);   
		            //System.out.println(activity.toString());
		            Object obj = JSON.parseObject(activity.toString(), clazz);
		            data.add(obj);
		     }
		     dataFileReader.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	     return new Values(data);
	}

	@Override
	public Fields getOutputFields() {
		return new Fields("datalist");
	}
	
}
