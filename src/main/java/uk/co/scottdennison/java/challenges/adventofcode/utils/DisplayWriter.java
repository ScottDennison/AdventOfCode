package uk.co.scottdennison.java.challenges.adventofcode.utils;

import java.io.IOException;
import java.io.Writer;

public class DisplayWriter extends Writer {
	private static final String NEWLINE_STRING = System.getProperty("line.separator");
	private static final char[] NEWLINE_CHARS = NEWLINE_STRING.toCharArray();
	private final Writer childWriter;
	private final char[] divider;
	private final char[] indent;
	private final boolean closeChildWriter;

	private boolean writtenStartDivider = false;
	private boolean indentPending = false;

	public DisplayWriter(Writer childWriter, String divider, String indent, boolean closeChildWriter) {
		this.childWriter = childWriter;
		this.divider = (indent + divider + NEWLINE_STRING).toCharArray();
		this.indent = indent.toCharArray();
		this.closeChildWriter = closeChildWriter;
	}

	@Override
	public void write(int c) throws IOException {
		if (!this.writtenStartDivider) {
			this.childWriter.write(this.divider);
			this.childWriter.flush();
			this.writtenStartDivider = true;
			this.indentPending = true;
		}
		if (c == '\r' || c == '\n') {
			this.indentPending = true;
		}
		else {
			if (this.indentPending) {
				this.childWriter.write(this.indent);
				this.indentPending = false;
			}
		}
		this.childWriter.write(c);
	}

	@Override
	public void write(char[] buffer, int offset, int count) throws IOException {
		if (count < 1) {
			return;
		}
		if (!this.writtenStartDivider) {
			this.childWriter.write(this.divider);
			this.writtenStartDivider = true;
			this.indentPending = true;
		}
		int bufferEnd = offset + count;
		int currentIndex = offset;
		while (currentIndex < bufferEnd) {
			// Consume all newlines
			int newlineSectionStart = currentIndex;
			while (currentIndex < bufferEnd) {
				char c = buffer[currentIndex];
				if (!(c == '\r' || c == '\n')) {
					break;
				}
				currentIndex++;
			}
			if (currentIndex > newlineSectionStart) {
				this.indentPending = true;
				this.childWriter.write(buffer, newlineSectionStart, currentIndex - newlineSectionStart);
			}
			if (currentIndex < bufferEnd) {
				if (this.indentPending) {
					this.childWriter.write(this.indent);
					this.indentPending = false;
				}
				// Consume all non-newlines
				int standardSectionStart = currentIndex;
				while (currentIndex < bufferEnd) {
					char c = buffer[currentIndex];
					if (c == '\r' || c == '\n') {
						break;
					}
					currentIndex++;
				}
				if (currentIndex > standardSectionStart) {
					this.childWriter.write(buffer, standardSectionStart, currentIndex - standardSectionStart);
				}
			}
		}
	}

	@Override
	public void flush() throws IOException {
		this.childWriter.flush();
	}

	@Override
	public void close() throws IOException {
		try {
			if (this.writtenStartDivider) {
				if (!this.indentPending) {
					this.childWriter.write(NEWLINE_CHARS);
				}
				this.childWriter.write(divider);
				this.childWriter.flush();
				this.indentPending = true;
			}
		} finally {
			if (this.closeChildWriter) {
				this.childWriter.close();
			}
		}
	}
}
