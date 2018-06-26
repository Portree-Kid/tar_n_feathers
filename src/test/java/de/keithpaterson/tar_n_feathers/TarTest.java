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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

/**
 * Unit test for {@link TarFile}.
 */
public class TarTest {
	/**
	 * Test reading of the header
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadHeader() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/e005n60.tar");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header = f.readHeader();
		String filename = header.getFilename();
		assertEquals("3040642.stg", filename);
		long size = header.getFilesize();
		assertEquals(297, size);
		f.close();
	}

	@Test
	public void testReadFileContent() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/e005n60.tar");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header = f.readHeader();
		String filename = header.getFilename();
		assertEquals("3040642.stg", filename);
		long size = header.getFilesize();
		assertEquals(297, size);
		byte[] content = f.getFileContent();
		assertEquals(size, content.length);
		String contentString = new String(content);
		f.close();
	}

	@Test
	public void testReadFileContentToDir() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/e005n60.tar");
		InputStream inputStream2 = getClass().getResourceAsStream("/3040642.stg");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header = f.readHeader();
		String filename = header.getFilename();
		assertEquals("3040642.stg", filename);
		long size = header.getFilesize();
		assertEquals(297, size);
		File dir = new File("e005n60");
		dir.mkdirs();
		f.writeFileContentToDir(dir);
		byte[] testContent = readFile(inputStream2);
		File writtenFile = new File(dir, "3040642.stg");
		byte[] content = readFile(writtenFile);
		writtenFile.delete();
		assertArrayEquals(testContent, content);
		String contentString = new String(content);
		f.close();
	}


	@Test
	public void testReadContinuous() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/e005n60.tar");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header;
		while ( (header = f.readHeader()) != null) {
			String filename = header.getFilename();
			long size = header.getFilesize();
			byte[] content = f.getFileContent();
			assertEquals(size, content.length);
			String contentString = new String(content);
		}
		f.close();
	}

	/**
	 * Test reading of the header
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadHeaderGzip() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/w158n20.tgz");
		TarFile f = new TarFile(new GZIPInputStream(inputStream));
		TarFileHeader header = f.readHeader();
		String filename = header.getFilename();
		assertEquals("367543.btg.gz", filename);
		long size = header.getFilesize();
		assertEquals(20899, size);
		f.close();
	}
	
	
	/**
	 * Reads the given File.
	 * 
	 * @param file
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */

	private byte[] readFile(File file) throws IOException {
		InputStream fis = new FileInputStream(file);
		return readFile(fis);
	}

	public byte[] readFile(InputStream fis) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int n = 0;
		byte[] buffer = new byte[8192];
		int off = 0;
		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				bos.write(buffer, 0, n);
				off += n;
			}
		}
		return bos.toByteArray();
	}

}
