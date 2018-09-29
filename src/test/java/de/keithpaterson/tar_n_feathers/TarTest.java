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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

/**
 * Unit test for {@link TarFile}.
 */
public class TarTest {
	
	private static final class DeleteVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return super.visitFile(file, attrs);
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return super.postVisitDirectory(dir, exc);
		}
	}

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
	public void testDirectories() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/roads.tar");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header;
		File currentDir = new File(".");
		while ( (header = f.readHeader()) != null) {
			long size = header.getFilesize();
			String filename = header.getFilename();
			f.writeFileContentToDir(currentDir);
		}
		f.close();
		File testFile = new File("Roads/w010n50/w003n55/2909248.stg");
		assertTrue(testFile.exists());
		Path path = Paths.get("Roads");
		Files.walkFileTree(path, new DeleteVisitor());
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
		Path path = Paths.get("e005n60");
		Files.walkFileTree(path, new DeleteVisitor());
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

	@Test
	public void testReadEffects() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/Effects.tar");
		TarFile f = new TarFile(inputStream);
		TarFileHeader header;
		while ( (header = f.readHeader()) != null) {
			String filename = header.getFilename();
			long size = header.getFilesize();
			byte[] content = f.getFileContent();
			assertEquals(size, content.length);
			String contentString = new String(content);
			System.out.println(size + header.getFilename());
		}
		f.close();
	}

	@Test
	public void testReadEffectsTgz() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/Effects.tgz");
		TarFile f = new TarFile(new GZIPInputStream(inputStream));
		TarFileHeader header;
		while ( (header = f.readHeader()) != null) {
			String filename = header.getFilename();
			long size = header.getFilesize();
			byte[] content = f.getFileContent();
			assertEquals(size, content.length);
			String contentString = new String(content);
			System.out.println(size + header.getFilename());
		}
		f.close();
	}

	@Test
	public void testReadEffectsBoth() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream("/Effects.tar");
		InputStream inputStream2 = getClass().getResourceAsStream("/Effects.tgz");
		TarFile f = new TarFile(inputStream);
		TarFile f2 = new TarFile(new GZIPInputStream(inputStream2));
		TarFileHeader header;
		TarFileHeader header2;
		while ( (header = f.readHeader()) != null && (header2 = f2.readHeader()) != null) {
			String filename = header.getFilename();
			assertHeaderEquals(header, header2);
			long size = header.getFilesize();
			byte[] content = f.getFileContent();
			byte[] content2 = f2.getFileContent();
			assertEquals(header.getFilesize(), header2.getFilesize());
			assertArrayEquals(content, content2);
			assertEquals(size, content.length);
			String contentString = new String(content);
			System.out.println(size + header.getFilename());
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
		fis.close();
		return bos.toByteArray();
	}
	
	private void assertHeaderEquals( TarFileHeader h1, TarFileHeader h2) {
		assertArrayEquals(h1.name, h2.name);
		assertArrayEquals(h1.mtime, h2.mtime);
		assertArrayEquals(h1.size, h2.size);
		assertArrayEquals(h1.magic, h2.magic);
		assertArrayEquals(h1.uid, h2.uid);
		assertArrayEquals(h1.chksum, h2.chksum);
		
	}
}
