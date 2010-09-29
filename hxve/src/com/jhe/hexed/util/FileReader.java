/* This file is part of HxVe.
 * 
 * HxVe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jhe.hexed.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {

	public int length = 0;
	private RandomAccessFile rndF;
	
	public int getLengthInt(){
		return (int) length;
	}
	
	/* Current position in file */
	private int curPos = 0;
	
	/**
	 * Read the byte at position n in the opened file. If no file is currently open,
	 * 0xff will be returned
	 * @param n
	 * @return
	 */
	public byte getByte(int n){
		byte ret = (byte) 255;
		
		if (rndF != null){

			try {
				if (!(n == curPos+1)){
					rndF.seek(n);
				}
				ret = rndF.readByte();
				curPos = n;
				
			} catch (IOException e) {e.printStackTrace();}
		}
		return ret;
	}
	
	public byte[] getBytes(int start, int end){
		byte[] ret = new byte[end-start];
		
		if (rndF != null){
			try {
				rndF.seek(start);
				rndF.read(ret);
			} catch (EOFException e) {
				System.out.println("err");
			} catch (IOException e) {e.printStackTrace();}
			
			return ret;
			
		} else {
			return null;
		}
	}
	
	public byte[] getBytesPadded(int start, int end, int length, byte padByte){
		byte[] ret = new byte[length];
		byte[] fromFile = getBytes(start, end);
		System.arraycopy(fromFile, 0, ret, 0, end-start);
		//Now fill up the rest with padding
		for (int i=end; i<length; i++){
			ret[i] = padByte;
		}
		
		return ret;
	}
	
	/**
	 * Returns if this filereader has a loaded file that can be read
	 * @return
	 */
	public boolean isValid(){
		if (rndF != null){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Write the specified data byte at position n in the file.
	 * @param data to write as a byte
	 * @param n Location where to write the byte
	 */
	public void writeByte(int n, byte data){
		if (rndF != null){
			try {
				if (!(n == curPos+1)){
					rndF.seek(n);
				}
				rndF.write(data);
				curPos++;
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public void openFile(File file){
		if (file != null){
		
			if (rndF != null){
				try{
					rndF.close();
				} catch (IOException e) {
				}//Ignore error, file was probably already closed
			}
			
			try {
				//fileIs = new FileInputStream(file);
				rndF = new RandomAccessFile(file, "rw"); 
			} catch (FileNotFoundException e) {e.printStackTrace();}

			// Get the size of the file
			this.length = (int)file.length();

			if (length > Integer.MAX_VALUE) {
				System.out.println("File is too large");
			}
			
		} else {
			System.out.println("No file specified");
		}
	}
	
	public void closeFile(){
        // Close the input stream and return bytes
        try {
			rndF.close();
			System.out.println("File closed");
		} catch (Exception e) {}
	}
}
