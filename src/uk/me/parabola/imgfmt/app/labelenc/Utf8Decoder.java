/*
 * Copyright (C) 2007 Steve Ratcliffe
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * 
 * Author: Steve Ratcliffe
 * Create date: Feb 19, 2008
 */
package uk.me.parabola.imgfmt.app.labelenc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Decoder for labels in utf-8, note that I am not actually sure that any
 * map uses utf-8.
 *
 * @author Steve Ratcliffe
 */
public class Utf8Decoder implements CharacterDecoder {
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private boolean needreset;

	private final Charset charset = Charset.forName("utf-8");

	/**
	 * Add a byte to this decoder.  This will be saved until a complete
	 * label string has been detected.
	 *
	 * @param b The byte read from the lbl file.
	 * @return True if a label string is finished and is ready to be retrieved
	 * via the {@link #getText} method.
	 */
	public boolean addByte(int b) {
		if (b == 0) {
			needreset = true;
			out.write(0);
			return true;
		}

		if (needreset) {
			out.reset();
			needreset = false;
		}

		out.write(b);
		return false;
	}

	/**
	 * Get the valid text.  This is guaranteed to be encoded as utf-8.
	 *
	 * @return The byte array and length as an EncodedText struct.
	 */
	public DecodedText getText() {
		byte[] ba = out.toByteArray();
		return new DecodedText(ba, charset);
	}

	/**
	 * Resets the state.  This should be called for example if the reader is
	 * jumping to a new place in the file and cannot guarantee that the previous
	 * label was fully read.
	 */
	public void reset() {
		needreset = false;
		out.reset();
	}
}
