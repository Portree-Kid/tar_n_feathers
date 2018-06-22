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

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class TarFile implements Closeable {

	private static final int HEADER = 0;
	private static final int FILE = 1;
	private static final int EOF = 2;

	private DataInputStream inputStream;

	/** Indicates reading of the header */
	int readState = HEADER;

	private TarFileHeader currentHeader;

	public TarFile(InputStream inputStream) {
		this.inputStream = new DataInputStream(inputStream);
	}

	public TarFileHeader readHeader() throws IOException {
		if (readState!=HEADER)
			throw new IllegalState("Not ready to read next header");
		TarFileHeader header = new TarFileHeader();
		try {
			header.read(inputStream);
		} catch (EOFException e) {
			currentHeader = null;
			readState = EOF;
			return null;
		}
		currentHeader = header;
		readState = FILE;
		return header;
	}

	public byte[] getFileContent() throws IOException {
		if( readState != FILE || currentHeader == null)
			throw new IllegalState("Not ready to read file");
		byte[] content = new byte[currentHeader.getFilesize()];
		inputStream.read(content);
		byte[] padding = new byte[512-content.length%512];
		inputStream.read(padding);
		readState = HEADER;
		return content;
	}

	public void close() throws IOException {
		inputStream.close();
	}

}
