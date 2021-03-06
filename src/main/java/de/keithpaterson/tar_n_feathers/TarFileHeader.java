/*
 * tar_n_feathers a lib to read ustar tar files.
 * Copyright (C) 2018  Keith Paterson (Portree Kid)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
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

package de.keithpaterson.tar_n_feathers;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Class to read a Tar file. <a href=
 * "https://www.gnu.org/software/tar/manual/html_node/Standard.html">File
 * format</a>
 * 
 * @author keith.paterson
 *
 */

public class TarFileHeader {

	/* Values used in typeflag field. */
	/** regular file */
	public static final char REGTYPE = '0';
	/** regular file */
	public static final char AREGTYPE = '\0';
	/** link */
	public static final char LNKTYPE = '1';
	/** reserved */
	public static final char SYMTYPE = '2';
	/** character special */
	public static final char CHRTYPE = '3';
	/** block special */
	public static final char BLKTYPE = '4';
	/** directory */
	public static final char DIRTYPE = '5';
	/** FIFO special */
	public static final char FIFOTYPE = '6';
	/** reserved */
	public static final char CONTTYPE = '7';
	byte[] name = new byte[100]; /* 0 */
	byte[] mode = new byte[8]; /* 100 */
	byte[] uid = new byte[8]; /* 108 */
	byte[] gid = new byte[8]; /* 116 */
	byte[] size = new byte[12]; /* 124 */
	byte[] mtime = new byte[12]; /* 136 */
	byte[] chksum = new byte[8]; /* 148 */
	byte[] typeflag = new byte[1]; /* 156 */
	byte[] linkname = new byte[100]; /* 157 */
	byte[] magic = new byte[6]; /* 257 */
	byte[] version = new byte[2]; /* 263 */
	byte[] uname = new byte[32]; /* 265 */
	byte[] gname = new byte[32]; /* 297 */
	byte[] devmajor = new byte[8]; /* 329 */
	byte[] devminor = new byte[8]; /* 337 */
	byte[] prefix = new byte[155]; /* 345 */

	/** Each tar file ends with two null blocks with 512 byte */
	byte[] endMarker = new byte[512];

	private int filesize;
	private String filename;
	
	/**
	 * Read the a Tar header for the next file in the archive.
	 * @param is The stream being read from
	 * @throws IOException
	 */

	public void read(InputStream is) throws IOException {
		byte[] header = new byte[512];
		readFullArray(is, header);

		// detect EOF (two 512 byte null markers)
		if (Arrays.equals(header, endMarker)) {
			readFullArray(is, header);
			if (Arrays.equals(header, endMarker))
				throw new EOFException();
			
			return;
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(header);
		readFullArray(bis, name);
		readFullArray(bis, mode);
		readFullArray(bis, uid);
		readFullArray(bis, gid);
		readFullArray(bis, size);
		readFullArray(bis,mtime);
		readFullArray(bis,chksum);
		readFullArray(bis,typeflag);
		readFullArray(bis,linkname);
		readFullArray(bis,magic);
		if (!new String(magic).trim().equals("ustar"))
			throw new UnsupportedFileFormat("Can only read ustar not " + new String(magic) );
		readFullArray(bis,version);
		readFullArray(bis,uname);
		readFullArray(bis,gname);
		readFullArray(bis,devmajor);
		readFullArray(bis,devminor);
		readFullArray(bis,prefix);
		bis.close();

		filename = new String(name, "ASCII");
		filename = filename.trim();

		if ((size[0] & 0x80) == 0x80) {
			throw new UnsupportedFileFormat("Only supports OctalFileSizes");

		} else {
			filesize = Integer.parseInt(new String(size, 0, 11), 8);
		}
	}

	public void readFullArray(InputStream is, byte[] header) throws IOException {
		long fPointer = 0;
		while (fPointer < header.length) {
			int readBytes = is.read(header, (int) fPointer, (int) Math.min(header.length - fPointer, 512));
			fPointer += readBytes;
		}
	}

	public synchronized int getFilesize() {
		return filesize;
	}

	public synchronized void setFilesize(int filesize) {
		this.filesize = filesize;
	}

	public synchronized String getFilename() {
		return filename;
	}

	public synchronized void setFilename(String filename) {
		this.filename = filename;
	}
}
