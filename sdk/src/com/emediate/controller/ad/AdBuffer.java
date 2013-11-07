/*
 * Created by Fredrik Hyttnäs-Lenngren on 7 nov 2013
 * Copyright (c) 2013 Emediate. All rights reserved.
 */
package com.emediate.controller.ad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;

/**
 * Cache-manager that holds a buffer of ads on the internal directory
 * 
 * @author Fredrik Hyttnäs-Lenngren
 * 
 */
public class AdBuffer {
    /**
     * Main directory under which ads will be cached
     */
    private final File mBufferDir;

    /**
     * Directory from which ads will be servered
     */
    private final File mCacheDir;

    /**
     * Create a new {@link AdBuffer}
     * 
     * @param context
     *            context used to access the file-system
     * @param campaign the campaign (url) which this AdBuffer will server           
     */
    public AdBuffer(Context context, String campaign) {
	mCacheDir = context.getCacheDir();
	final String hashedName = Long.toString(campaign.hashCode() & 0xffffffffL, Character.MAX_RADIX);
	mBufferDir = context.getDir(hashedName, Context.MODE_PRIVATE);
    }

    /**
     * Return true if the buffer is empty
     */
    public boolean isEmpty() {
	try {
	    listBuffer();
	    return false;
	} catch (IOException e) {
	    return true;
	}
    }

    /**
     * Clears the buffer
     * 
     * @return number of ads which was removed
     */
    public int clear() {
	try {
	    final File[] files = listBuffer();
	    for (File file : files)
		file.delete();
	    
	    return files.length;
	} catch (FileNotFoundException e) {
	    return 0;
	}
    }

    /**
     * Return the next ad in the buffer, removing it from the buffer in the proccess
     * 
     * @return a file which points to the ad
     * @throws IOException
     *             if the cach is empty or if an error occurs
     */
    File pop() throws IOException {
	final File[] buffer = listBuffer();

	Arrays.sort(buffer, new LastModifiedComparator());

	final File oldest = buffer[0];
	final File temp = peep();
	if(!oldest.renameTo(temp))
	    throw new IOException("Unable to open file");

	return temp;
    }

    /**
     * Return a list of all ads which have been cached
     * 
     * @return list of cached ads
     * @throws FileNotFoundException
     *             if buffer is empty
     */
    private File[] listBuffer() throws FileNotFoundException {
	final File[] buffer = mBufferDir.listFiles();
	if (buffer == null || buffer.length == 0)
	    throw new FileNotFoundException("Cache is empty");

	return buffer;
    }

    /**
     * Return a File pointing to the last servered ad which was retrived via
     * {@link #pop()}
     * 
     * @return the file
     */
    File peep() {
	return new File(mCacheDir, mBufferDir.getName() + ".html");
    }

    /**
     * Place the given ad in the buffer, placing it last
     * 
     * @param input
     *            inputstream to ad which should be buffered. <br>
     *            It's up to the called to close the stream.
     * @throws IOException
     *             if unable to cache
     */
    void put(InputStream input) throws IOException {
	try {
	    final File file = new File(mBufferDir, generateName());
	    copyToFile(input, file);
	} catch (FileNotFoundException e) {
	    throw new IOException(e.getMessage());
	}
    }

    /**
     * Return a name under which a buffered ad can be stored
     */
    protected String generateName() {
	return mBufferDir.getName() + "_" + System.currentTimeMillis();
    }

    /**
     * Copy from the given {@link InputStream} to the given
     * {@link FileNotFoundException}
     * 
     * @param input
     *            input to read from
     * @param file
     *            file to copy to
     * @throws FileNotFoundException
     * @throws IOException
     */
    void copyToFile(InputStream input, final File file) throws FileNotFoundException, IOException {
	final FileOutputStream out = new FileOutputStream(file);

	final byte buffer[] = new byte[1024];
	int lenght;
	while ((lenght = input.read(buffer, 0, 1024)) != -1) {
	    out.write(buffer, 0, lenght);
	}
	out.flush();
	out.close();
    }

    /**
     * {@link Comparator} for sorting {@link File} according to
     * {@link File#lastModified()}
     * 
     * @author Fredrik Hyttnäs-Lenngren
     * 
     */
    class LastModifiedComparator implements Comparator<File> {

	@Override
	public int compare(File lhs, File rhs) {
	    if (lhs.lastModified() == rhs.lastModified()) {
		return 0;
	    } else if (lhs.lastModified() < rhs.lastModified()) {
		return 1;
	    } else {
		return -1;
	    }
	}

    }
}
