/*
 * Copyright (C) 2010.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 or
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package uk.me.parabola.imgfmt.app.srt;

import java.io.IOException;
import java.nio.charset.Charset;

import uk.me.parabola.imgfmt.app.BufferedImgFileWriter;
import uk.me.parabola.imgfmt.app.ImgFile;
import uk.me.parabola.imgfmt.app.ImgFileWriter;
import uk.me.parabola.imgfmt.app.SectionWriter;
import uk.me.parabola.imgfmt.fs.ImgChannel;
import uk.me.parabola.imgfmt.sys.FileImgChannel;
import uk.me.parabola.mkgmap.srt.SrtTextReader;

/**
 * The SRT file. This contains a table showing the sort order of
 * the characters that is being used.
 *
 * @author Steve Ratcliffe
 */
public class SRTFile extends ImgFile {

	private final SRTHeader header;
	private String description;

	public SRTFile(ImgChannel chan) {
		header = new SRTHeader();
		setHeader(header);

		BufferedImgFileWriter fileWriter = new BufferedImgFileWriter(chan);
		fileWriter.setMaxSize(Long.MAX_VALUE);
		setWriter(fileWriter);

		// Position at the start of the writable area.
		position(header.getHeaderLength());
	}

	/**
	 * Write out the file.
	 * This file has an unusual layout. There are several header like structures within
	 * the main body of the file, with the real header being very small.
	 */
	public void write() {
		ImgFileWriter writer = getWriter();
		writeDescription(writer);

		SectionWriter subWriter = header.makeSectionWriter(writer);
		subWriter.position(SRTHeader.HEADER3_LEN);
		writeCharacterTable(subWriter);
		writeTab2(subWriter);
		subWriter.close();

		// Header 2 is just after the real header
		writer.position(header.getHeaderLength());
		header.writeHeader2(writer);

		// Header 3 is after the description
		writer.position(header.getHeaderLength() + description.length() + 1 + SRTHeader.HEADER2_LEN);
		header.writeHeader3(writer);

		header.writeHeader(writer);
	}

	private void writeDescription(ImgFileWriter writer) {
		writer.position(header.getHeaderLength() + SRTHeader.HEADER2_LEN);
		writer.put(description.getBytes(Charset.forName("ascii")));
		writer.put((byte) 0);
		header.endDescription(writer.position());
	}

	private void writeCharacterTable(ImgFileWriter writer) {
		header.endCharTable(writer.position());
	}

	private void writeTab2(ImgFileWriter writer) {
		header.endTab2(writer.position());
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCodepage(int cp) {
		header.setCodepage((char) cp);
	}

	public static void main(String[] args) throws IOException {
		ImgChannel chan = new FileImgChannel("foo.srt");
		SRTFile sf = new SRTFile(chan);

		SrtTextReader tr = new SrtTextReader("/opt/data/1252.txt");
		sf.setCodepage(tr.getCodepage());
		sf.setDescription(tr.getDescription());
		sf.write();
		sf.close();
		chan.close();
	}
}
