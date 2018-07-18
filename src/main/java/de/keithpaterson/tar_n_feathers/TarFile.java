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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to read a Tar file. <a href=
 * "https://www.gnu.org/software/tar/manual/html_node/Standard.html">File
 * format</a>
 * 
 * @author keith.paterson
 *
 */

public class TarFile implements Closeable {

	/** The next possible operation is to read the 512 byte header. */
	private static final int HEADER = 0;
	/** The next possible operation is to get the content of the file */
	private static final int FILE = 1;
	private static final int EOF = 2;

	/** The stream being read from. */
	private DataInputStream inputStream;

	/** Indicates the current state. */
	int readState = HEADER;

	/** The header of the next file that can be read. */
	private TarFileHeader currentHeader;

	public TarFile(InputStream inputStream) {
		this.inputStream = new DataInputStream(inputStream);
	}

	/**
	 * Read the next 512 Byte header and set the state to FILE. EOF is indicated by
	 * returning null;
	 * 
	 * @return
	 * @throws IOException
	 */

	public TarFileHeader readHeader() throws IOException {
		if (readState != HEADER)
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

	/**
	 * Returns the content of the file described by currentHeader.
	 * 
	 * @return
	 * @throws IOException
	 */

	public byte[] getFileContent() throws IOException {
		if (readState != FILE || currentHeader == null || currentHeader.typeflag[0] == TarFileHeader.DIRTYPE)
			throw new IllegalState("Not ready to read file");
		byte[] content = new byte[currentHeader.getFilesize()];
		inputStream.read(content);
		byte[] padding = new byte[512 - content.length % 512];
		inputStream.read(padding);
		readState = HEADER;
		return content;
	}

	/**
	 * Writes the file contents to a File or creates the directory.
	 * 
	 * @param dir
	 *            The directory to write the file to.
	 * @throws IOException
	 */
	public void writeFileContentToDir(File dir) throws IOException {
		if (readState != FILE || currentHeader == null)
			throw new IllegalState("Not ready to read/write file");
		if (currentHeader.typeflag[0] == TarFileHeader.DIRTYPE) {
			File newDir = new File(dir, currentHeader.getFilename());
			if (!newDir.exists()) {
				boolean result = newDir.mkdirs();
				if (!result)
					throw new RuntimeException();
			}
			readState = HEADER;
			return;
		}
		long fLen = currentHeader.getFilesize();
		long fPointer = 0;
		byte[] buffer = new byte[512];
		FileOutputStream fos = new FileOutputStream(new File(dir, currentHeader.getFilename()));
		while (fPointer < fLen) {
			int readBytes = inputStream.read(buffer, 0, (int) Math.min(fLen - fPointer, 512));
			fos.write(buffer, 0, readBytes);
			fPointer += readBytes;
		}
		fos.close();
		byte[] padding = new byte[(int) (512 - fLen % 512)];
		inputStream.read(padding);
		readState = HEADER;
	}

	/**
	 * The current state of file reading.
	 * 
	 * @see TarFile.HEADER
	 * @see TarFile.FILE
	 * @see TarFile.EOF
	 * @return
	 */
	public int getReadState() {
		return readState;
	}

	public TarFileHeader getCurrentHeader() {
		return currentHeader;
	}

	public void close() throws IOException {
		inputStream.close();
	}

}
