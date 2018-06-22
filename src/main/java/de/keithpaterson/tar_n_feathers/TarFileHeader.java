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

public class TarFileHeader {
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

	byte[] endMarker = new byte[512];

	private int filesize;
	private String filename;
	private int endMarkers = 0;

	public void read(InputStream is) throws IOException {
		byte[] header = new byte[512];
		is.read(header);

		if (Arrays.equals(header, endMarker)) {
			endMarkers++;
			is.read(header);
			if(Arrays.equals(header, endMarker))
				throw new EOFException();
		}		

		ByteArrayInputStream bis = new ByteArrayInputStream(header);
		bis.read(name);
		bis.read(mode);
		bis.read(uid);
		bis.read(gid);
		bis.read(size);
		bis.read(mtime);
		bis.read(chksum);
		bis.read(typeflag);
		bis.read(linkname);
		bis.read(magic);
		if (!new String(magic).trim().equals("ustar"))
			throw new UnsupportedFileFormat("Can only read ustar");
		bis.read(version);
		bis.read(uname);
		bis.read(gname);
		bis.read(devmajor);
		bis.read(devminor);
		bis.read(prefix);
		bis.close();

		filename = new String(name, "ASCII");
		filename = filename.trim();

		if ((size[0] & 0x80) == 0x80) {
			throw new UnsupportedFileFormat("Only supports OctalFileSizes");

		} else {
			filesize = Integer.parseUnsignedInt(new String(size, 0, 11), 8);
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
