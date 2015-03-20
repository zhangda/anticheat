package cn.rfidcn.anticheat.scheme;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
		//getFile(bytes, "C:\\Users\\zhang\\Desktop","avrofile");
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
		            System.out.println("=========="+activity.toString());
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
	
	
	 public void getFile(byte[] bfile, String filePath,String fileName) {  
	        BufferedOutputStream bos = null;  
	        FileOutputStream fos = null;  
	        File file = null;  
	        try {  
	            File dir = new File(filePath);  
	            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在  
	                dir.mkdirs();  
	            }  
	            file = new File(filePath+"\\"+fileName);  
	            fos = new FileOutputStream(file);  
	            bos = new BufferedOutputStream(fos);  
	            bos.write(bfile);  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        } finally {  
	            if (bos != null) {  
	                try {  
	                    bos.close();  
	                } catch (IOException e1) {  
	                    e1.printStackTrace();  
	                }  
	            }  
	            if (fos != null) {  
	                try {  
	                    fos.close();  
	                } catch (IOException e1) {  
	                    e1.printStackTrace();  
	                }  
	            }  
	        }  
	    }  
 

}
