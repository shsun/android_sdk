package com.emediate.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.StatFs;

import com.emediate.view.MraidView;

/**
 * The Class OrmmaAssetController. This class handles asset management for ormma
 */
public class MraidAssetController extends MraidController {

	/**
	 * Instantiates a new ormma asset controller.
	 * 
	 * @param adView
	 *            the ad view
	 * @param c
	 *            the c
	 */
	private Context mContext;
	public MraidAssetController(MraidView adView, Context c) {
		super(adView, c);
		this.mContext = c;
	}
	/**
	 * Copy text file from jar into asset directory.
	 * 
	 * @param alias
	 *            the alias to store it in
	 * @param source
	 *            the source
	 * @return the path to the copied asset
	 */
	public String copyTextFromJarIntoAssetDir(String alias, String source) {
		try {
			InputStream in = MraidAssetController.class.getResourceAsStream(source);
			File writeFile = new File(mContext.getFilesDir(), alias);
			return writeToDisk(in, writeFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String writeToDisk(InputStream in, File writeFile) throws IllegalStateException, IOException {
		byte buff[] = new byte[1024];
		FileOutputStream out = new FileOutputStream(writeFile);

		do {
			int numread = in.read(buff);
			if (numread <= 0)
				break;
			out.write(buff, 0, numread);
			
		} while (true);
		
		out.flush();
		out.close();
		
		in.close(); 
		
		return writeFile.getAbsolutePath();
	}
	
	
	/**
	 * Adds an asset.
	 * 
	 * @param alias
	 *            the alias
	 * @param url
	 *            the url
	 */
	public void addAsset(String alias, String url) {
		HttpEntity entity = getHttpEntity(url);
		InputStream in = null;
		try {
			in = entity.getContent();
			writeToDisk(in, alias, false);
			String str = "MraidAdController.addedAsset('" + alias + "' )";
			mMraidView.injectJavaScript(str);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				in = null;
			}

		}
		try {
			entity.consumeContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * pulls a resource from the web
	 * get the http entity at a given url
	 * 
	 * @param url
	 *            the url
	 * @return the http entity
	 */
	private HttpEntity getHttpEntity(String url)
	{
		HttpEntity entity = null;
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			entity = response.getEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return entity;
	}

	/**
	 * Cache remaining.
	 * 
	 * @return the cache remaining
	 */
	public int cacheRemaining() {
		File filesDir = mContext.getFilesDir();
		StatFs stats = new StatFs(filesDir.getPath());
		int free = stats.getFreeBlocks() * stats.getBlockSize();
		return free;
	}

	/**
	 * Write a stream to disk.
	 * writes a HTTP entity to the specified filename and location on disk
	 * 
	 * @param in
	 *            the input stream
	 * @param file
	 *            the file to store it in
	 * @param storeInHashedDirectory
	 *            use a hashed directory name
	 * @return the path where it was stired
	 * @throws IllegalStateException
	 *             the illegal state exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String writeToDisk(InputStream in, String file,
			boolean storeInHashedDirectory) throws IllegalStateException,
			IOException
	{
		byte buff[] = new byte[1024];

		MessageDigest digest = null;
		if (storeInHashedDirectory) {
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream out = null;
		try {
			out = getAssetOutputString(file);
			do {
				int numread = in.read(buff);
				if (numread <= 0)
					break;

				if (storeInHashedDirectory && digest != null) {
					digest.update(buff);
				}
				out.write(buff, 0, numread);
			} while (true);
			out.flush();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
				out = null;
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				in = null;
			}
		}
		String filesDir = getFilesDir();

		if (storeInHashedDirectory && digest != null) {
			filesDir = moveToAdDirectory(file, filesDir, asHex(digest));
		}
		return filesDir + file;

	}

	/**
	 * Write an input stream to a file wrapping it with ormma stuff
	 * writes a HTTP entity to the specified filename and location on disk
	 * 
	 * @param in
	 *            the input stream
	 * @param file
	 *            the file to store it in
	 * @param storeInHashedDirectory
	 *            use a hashed directory name
	 * @return the path where it was stored
	 * @throws IllegalStateException
	 *             the illegal state exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String writeToDiskWrap(MraidView view, InputStream in, String file,
			boolean storeInHashedDirectory, String injection,
			String bridgePath, String mraidPath) throws IllegalStateException,
			IOException
	{
		byte buff[] = new byte[1024*2];

		MessageDigest digest = null;
		if (storeInHashedDirectory) {
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	
		// check for html tag in the input
		ByteArrayOutputStream fromFile = new ByteArrayOutputStream();
		FileOutputStream out = null;
		
		try {
			do {
				int numread = in.read(buff);

				if (numread <= 0) {
					break;
				}

				if (storeInHashedDirectory && digest != null) {
					digest.update(buff);
				}

				fromFile.write(buff, 0, numread);

			} while (true);

			String wholeHTML = fromFile.toString();
			boolean hasHTMLWrap = wholeHTML.indexOf("</html>") >= 0;

			// Cannot have injection when full html
			StringBuffer wholeHTMLBuffer = null;
			if (hasHTMLWrap) {
				wholeHTMLBuffer = new StringBuffer(wholeHTML);
				int start = wholeHTMLBuffer.indexOf("/mraid_bridge.js");
				if (start <= 0) {
				}else{
					wholeHTMLBuffer.replace(start, start + "/mraid_bridge.js".length(), "file:/" + bridgePath);
				}
				start = wholeHTMLBuffer.indexOf("/mraid.js");
				
				if (start <= 0) {
				}else{
					wholeHTMLBuffer.replace(start, start + "/mraid.js".length(), "file:/" + mraidPath);
				}
				
				// Retreive Ads Default Width and Height
				int defaultStart = wholeHTML.indexOf("EmediateDefaultWidthAndHeightStart");
				int defaultEnd = wholeHTML.indexOf("EmediateDefaultWidthAndHeightEnd");
				
				if (defaultStart <= 0 || defaultEnd <= 0) {
				}else{
					String defaultWidthAndHeight = wholeHTML.substring(defaultStart + "EmediateDefaultWidthAndHeightStart".length()+1, defaultEnd-1).trim();
					float width = Float.parseFloat(defaultWidthAndHeight.split(",")[0]);
					float height = Float.parseFloat(defaultWidthAndHeight.split(",")[1]);
					
					view.setAdsDefaultWidth(width);
					view.setAdsDefaultHeight(height);
					
				}
				
			}else{
				// Retreive Ads Default Width and Height
				int defaultStart = wholeHTML.indexOf("EmediateDefaultWidthAndHeightStart");
				int defaultEnd = wholeHTML.indexOf("EmediateDefaultWidthAndHeightEnd");
				
				if (defaultStart <= 0 || defaultEnd <= 0) {
				}else{
					String defaultWidthAndHeight = wholeHTML.substring(defaultStart + "EmediateDefaultWidthAndHeightStart".length()+1, defaultEnd-1).trim();
					float width = Float.parseFloat(defaultWidthAndHeight.split(",")[0]);
					float height = Float.parseFloat(defaultWidthAndHeight.split(",")[1]);
					
					view.setAdsDefaultWidth(width);
					view.setAdsDefaultHeight(height);
					
				}
			}
			
			out = getAssetOutputString(file);
			
			if (!hasHTMLWrap) {
				out.write("<html>".getBytes());
				out.write("<head>".getBytes());
				out.write("<meta name='viewport' content='user-scalable=no initial-scale=1.0' />"
						.getBytes());
				out.write("<title>Advertisement</title> ".getBytes());

				out.write(("<script src=\"file://" + bridgePath + "\" type=\"text/javascript\"></script>")
						.getBytes());
				out.write(("<script src=\"file://" + mraidPath + "\" type=\"text/javascript\"></script>")
						.getBytes());
				
				
				// Include third-party data to html file if it exists
				if (injection != null) {
					out.write("<script type=\"text/javascript\">".getBytes());
					out.write(injection.getBytes());
					out.write("</script>".getBytes());
				}
				
				
				out.write("</head>".getBytes());
				out.write("<body style=\"margin:0; padding:0; overflow:hidden; background-color:transparent;\">".getBytes());
				out.write("<div align=\"center\"> ".getBytes());
				
			}

			if (!hasHTMLWrap) {
				out.write(fromFile.toByteArray());
				
				
			} else {
				out.write(wholeHTMLBuffer.toString().getBytes());
			}

			if (!hasHTMLWrap) {
				out.write("</div> ".getBytes());
				out.write("</body> ".getBytes());
				out.write("</html> ".getBytes());
				
			}

			out.flush();
			
		} finally {
              if(fromFile != null){
            	  try{
            		   fromFile.close();
            	  }catch (Exception e) {
				}
            	  fromFile = null;
              }
              if(out != null){
            	  try{
            		  out.close();
            	  }
            	  catch (Exception e) {
				}
            	 out = null;
              }
              if(in != null){
            	  try{
            		  in.close();
            	  }
            	  catch (Exception e) {
				}
            	 in = null;
              }
		}
				
		String filesDir = getFilesDir();
		if (storeInHashedDirectory && digest != null) {
			filesDir = moveToAdDirectory(file, filesDir, asHex(digest));
		}
		return filesDir;
	}
	
	/**
	 * Generate html File 
	 * Debug
	 */
	public String unusewriteToDiskWrap(InputStream in, String file,
			boolean storeInHashedDirectory, String injection,
			String bridgePath, String mraidPath) throws IllegalStateException,
			IOException
	/**
	 * writes a HTTP entity to the specified filename and location on disk
	 */
	{
		byte buff[] = new byte[1024];

		MessageDigest digest = null;
		if (storeInHashedDirectory) {
			try {
				digest = java.security.MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		
		// check for html tag in the input
		ByteArrayOutputStream fromFile = new ByteArrayOutputStream();

		File myFile = null;
		// Get File Directory
		File SDFile = android.os.Environment.getExternalStorageDirectory();
		File destDir = new File(SDFile.getAbsolutePath() + "/Android/data/com.mraid.android/files/");
		if (!destDir.exists())
			destDir.mkdirs();
		
		// Open File
		myFile = new File(destDir + File.separator + file + ".html");
		// CHeck if the file is exist, if not, create
		if (myFile.exists() && true) {
			myFile.delete();
		}
		if (!myFile.exists()) {
			myFile.createNewFile();
		}
		// Write Text into file
		FileOutputStream outputStream = new FileOutputStream(myFile);
				
		try {
			do {
				int numread = in.read(buff);

				if (numread <= 0) {
					break;
				}

				if (storeInHashedDirectory && digest != null) {
					digest.update(buff);
				}

				fromFile.write(buff, 0, numread);

			} while (true);

			String wholeHTML = fromFile.toString();
			boolean hasHTMLWrap = wholeHTML.indexOf("</html>") >= 0;
			// Cannot have injection when full html
			StringBuffer wholeHTMLBuffer = null;

			if (hasHTMLWrap) {
				wholeHTMLBuffer = new StringBuffer(wholeHTML);

				int start = wholeHTMLBuffer.indexOf("/mraid_bridge.js");
				if (start <= 0) {}
				wholeHTMLBuffer.replace(start, start + "/mraid_bridge.js".length(), "file:/" + bridgePath);
				start = wholeHTMLBuffer.indexOf("/mraid.js");
				if (start <= 0) {}
				wholeHTMLBuffer.replace(start, start + "/mraid.js".length(), "file:/" + mraidPath);
			}
			
			if (!hasHTMLWrap) {
				outputStream.write("<html>".getBytes());
				outputStream.write("<head>".getBytes());
				outputStream.write("<meta name='viewport' content='user-scalable=no initial-scale=1.0' />"
						.getBytes());
				outputStream.write("<title>Advertisement</title> ".getBytes());

				outputStream.write(("<script src=\"file:/" + bridgePath + "\" type=\"text/javascript\"></script>")
						.getBytes());
				outputStream.write(("<script src=\"file:/" + mraidPath + "\" type=\"text/javascript\"></script>")
						.getBytes());
			
				outputStream.write("</head>".getBytes());
				outputStream.write("<body style=\"margin:0; padding:0; overflow:hidden; background-color:transparent;\">".getBytes());
				outputStream.write("<div align=\"center\"> ".getBytes());
			}

			if (!hasHTMLWrap) {
				outputStream.write(fromFile.toByteArray());
			} else {
				outputStream.write(wholeHTMLBuffer.toString().getBytes());
			}

			if (!hasHTMLWrap) {
				outputStream.write("</div> ".getBytes());
				outputStream.write("</body> ".getBytes());
				outputStream.write("</html> ".getBytes());
			}

			outputStream.flush();
			outputStream.close();
		} finally {
              if(fromFile != null){
            	  try{
            		   fromFile.close();
            	  }catch (Exception e) {
				}
            	  fromFile = null;
              }
              if(in != null){
            	  try{
            		  in.close();
            	  }
            	  catch (Exception e) {
				}
            	 in = null;
              }
		}
				
		String filesDir = getFilesDir();
		if (storeInHashedDirectory && digest != null) {
			filesDir = moveToAdDirectory(file, filesDir, asHex(digest));
		}
		return filesDir;
	}


	/**
	 * Move a file to ad directory.
	 * 
	 * @param fn
	 *            the filename
	 * @param filesDir
	 *            the files directory
	 * @param subDir
	 *            the sub directory
	 * @return the path where it was stored
	 */
	private String moveToAdDirectory(String fn, String filesDir, String subDir) {
		File file = new File(filesDir + java.io.File.separator + fn);
		File adDir = new File(filesDir + java.io.File.separator + "ad");
		adDir.mkdir();
		File dir = new File(filesDir + java.io.File.separator + "ad"
				+ java.io.File.separator + subDir);
		dir.mkdir();
		file.renameTo(new File(dir, file.getName()));
		return dir.getPath() + java.io.File.separator;
	}

	/**
	 * The Constant HEX_CHARS.
	 */
	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };

	/**
	 * Builds a hex string
	 * 
	 * @param digest
	 *            the digest
	 * @return the string
	 */
	private String asHex(MessageDigest digest) {
		byte[] hash = digest.digest();
		char buf[] = new char[hash.length * 2];
		for (int i = 0, x = 0; i < hash.length; i++) {
			buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
			buf[x++] = HEX_CHARS[hash[i] & 0xf];
		}
		return new String(buf);
	}

	/**
	 * Gets the files dir for the activity.
	 * 
	 * @return the files dir
	 */
	private String getFilesDir() {
		return mContext.getFilesDir().getPath();
	}

	/**
	 * Gets the asset output string.
	 * 
	 * @param asset
	 *            the asset
	 * @return the asset output string
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public FileOutputStream getAssetOutputString(String asset)
			throws FileNotFoundException {
		File dir = getAssetDir(getAssetPath(asset));
		dir.mkdirs();
		File file = new File(dir, getAssetName(asset));
		return new FileOutputStream(file);
	}

	/**
	 * Removes the asset.
	 * 
	 * @param asset
	 *            the asset
	 */
	public void removeAsset(String asset) {
		File dir = getAssetDir(getAssetPath(asset));
		dir.mkdirs();
		File file = new File(dir, getAssetName(asset));
		file.delete();

		String str = "MraidAdController.assetRemoved('" + asset + "' )";
		mMraidView.injectJavaScript(str);
	}

	/**
	 * Gets the asset dir.
	 * 
	 * @param path
	 *            the path
	 * @return the asset dir
	 */
	private File getAssetDir(String path) {
		File filesDir = mContext.getFilesDir();
		File newDir = new File(filesDir.getPath() + java.io.File.separator
				+ path);
		return newDir;
	}

	/**
	 * Gets the asset path.
	 * 
	 * @param asset
	 *            the asset
	 * @return the asset path
	 */
	private String getAssetPath(String asset) {
		int lastSep = asset.lastIndexOf(java.io.File.separatorChar);
		String path = "/";

		if (lastSep >= 0) {
			path = asset.substring(0,
					asset.lastIndexOf(java.io.File.separatorChar));
		}
		return path;
	}

	/**
	 * Gets the asset name.
	 * 
	 * @param asset
	 *            the asset
	 * @return the asset name
	 */
	private String getAssetName(String asset) {
		int lastSep = asset.lastIndexOf(java.io.File.separatorChar);
		String name = asset;

		if (lastSep >= 0) {
			name = asset.substring(asset
					.lastIndexOf(java.io.File.separatorChar) + 1);
		}
		return name;
	}

	/**
	 * Gets the asset path.
	 * 
	 * @return the asset path
	 */
	public String getAssetPath() {
		return "file://" + mContext.getFilesDir() + "/";
	}

	/**
	 * Delete directory.
	 * 
	 * @param path
	 *            the path
	 * @return true, if successful
	 */
	static public boolean deleteDirectory(String path) {
		if (path != null)
			return deleteDirectory(new File(path));
		return false;
	}

	/**
	 * Delete directory.
	 * 
	 * @param path
	 *            the path
	 * @return true, if successful
	 */
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 * Delete old ads.
	 */
	public void deleteOldAds() {
		String filesDir = getFilesDir();
		File adDir = new File(filesDir + java.io.File.separator + "ad");
		deleteDirectory(adDir);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ormma.controller.OrmmaController#stopAllListeners()
	 */
	@Override
	public void stopAllListeners() {
	}
}
