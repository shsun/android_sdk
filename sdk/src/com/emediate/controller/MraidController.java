package com.emediate.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.emediate.controller.util.NavigationStringEnum;
import com.emediate.controller.util.TransitionStringEnum;
import com.emediate.view.MraidView;

/**
 * Abstract class for all controller objects
 * Controller objects implement pieces of the java/javascript interface
 */
public abstract class MraidController {

	//view it is attached to
	protected MraidView mMraidView;
	//context it is in
	protected Context mContext;

	//class types for converting JSON
	private static final String STRING_TYPE = "class java.lang.String";
	private static final String INT_TYPE = "int";
	private static final String BOOLEAN_TYPE = "boolean";
	private static final String FLOAT_TYPE = "float";
	private static final String NAVIGATION_TYPE = "class com.emediate.mraid.controller.NavigationStringEnum";
	private static final String TRANSITION_TYPE = "class com.emediate.mraid.controller.TransitionStringEnum";
	
	public static final String FULL_SCREEN = "fullscreen";
	public static final String EXIT = "exit";
	public static final String STYLE_NORMAL = "normal";

	/**
	 * 
	 * Contains audio and video properties
	 *
	 */
	public static class PlayerProperties extends ReflectedParcelable {
			
		
		public PlayerProperties() {
			autoPlay = showControl = true;
			doLoop = audioMuted = false;
			startStyle = stopStyle = STYLE_NORMAL;
			inline = false;			
		}
		
		/**
		 * The Constant CREATOR.
		 */
		public static final Parcelable.Creator<PlayerProperties> CREATOR = new Parcelable.Creator<PlayerProperties>() {
			public PlayerProperties createFromParcel(Parcel in) {
				return new PlayerProperties(in);
			}

			public PlayerProperties[] newArray(int size) {
				return new PlayerProperties[size];
			}
		};		
		
		public PlayerProperties(Parcel in) {
			super(in);
		}

		/**
		 * Set stop style
		 * @param style - stop style (normal/full screen)
		 */
		public void setStopStyle(String style){
			stopStyle = style;
		}
		
		/**
		 * Set Player properties
		 * @param autoPlay - true if player should start immediately
		 * @param controls - true if player should show controls
		 * @param loop - true if player should start again after finishing
		 */
		public void setProperties(boolean audioMuted, boolean autoPlay, boolean controls, boolean inline,boolean loop, String startStyle, String stopStyle){
			this.autoPlay = autoPlay;
			this.showControl = controls;
			this.doLoop = loop;
			this.audioMuted = audioMuted;
			this.startStyle = startStyle;
			this.stopStyle = stopStyle;
			this.inline = inline;			

		}
		
		/**
		 * Mute Audio
		 */
		public void muteAudio(){
			audioMuted = true;
		}
		
		/**
		 * Get autoPlay
		 * 
		 */
		public boolean isAutoPlay(){
			return (autoPlay == true);
		}
		
		/**
		 * Get show control
		 */
		public boolean showControl(){
			return showControl;
		}
		
		/**
		 * 
		 * Get looping option
		 */
		public boolean doLoop(){
			return doLoop;
		}
		
		/**
		 * Get mute status
		 */
		public boolean doMute(){
			return audioMuted;
		}
		
		/**
		 * 
		 * Get stop style
		 */
		public boolean exitOnComplete(){
			return stopStyle.equalsIgnoreCase(EXIT);
		}
		
		/**
		 * 
		 * Get start style
		 */
		public boolean isFullScreen(){
			return startStyle.equalsIgnoreCase(FULL_SCREEN);
		}		
		
		public boolean autoPlay, showControl, doLoop, audioMuted,inline;
		public String stopStyle, startStyle;
	}
	
	/**
	 * The Class Dimensions.  Holds dimensions coming from javascript
	 */
	public static class Dimensions extends ReflectedParcelable {

		/**
		 * Instantiates a new dimensions.
		 */
		public Dimensions() {
			x = -1;
			y = -1;
			width = -1;
			height = -1;
		};

		/**
		 * The Constant CREATOR.
		 */
		public static final Parcelable.Creator<Dimensions> CREATOR = new Parcelable.Creator<Dimensions>() {
			public Dimensions createFromParcel(Parcel in) {
				return new Dimensions(in);
			}

			public Dimensions[] newArray(int size) {
				return new Dimensions[size];
			}
		};

		/**
		 * Instantiates a new dimensions from a parcel.
		 *
		 * @param in the in
		 */
		protected Dimensions(Parcel in) {
			super(in);
		}
		

		/**
		 * The dimenstion values
		 */
		public int x, y, width, height;

	}

	/**
	 * The Class Properties for holding properties coming from javascript
	 */
	public static class Properties extends ReflectedParcelable {
		
		/**
		 * Instantiates a new properties from a parcel
		 *
		 * @param in the in
		 */
		protected Properties(Parcel in) {
			super(in);
		}

		/**
		 * Instantiates a new properties.
		 */
		public Properties() {
			useBackground = false;
			backgroundColor = 0;
			backgroundOpacity = 0;
		};

		/**
		 * The Constant CREATOR.
		 */
		public static final Parcelable.Creator<Properties> CREATOR = new Parcelable.Creator<Properties>() {
			public Properties createFromParcel(Parcel in) {
				return new Properties(in);
			}

			public Properties[] newArray(int size) {
				return new Properties[size];
			}
		};
		
		//property values
		public boolean useBackground;
		public int backgroundColor;
		public float backgroundOpacity;
	}

	/**
	 * Instantiates a new ormma controller.
	 *
	 * @param adView the ad view
	 * @param context the context
	 */
	public MraidController(MraidView adView, Context context) {
		mMraidView = adView;
		mContext = context;
	}

	/**
	 * Constructs an object from json via reflection
	 *
	 * @param json the json
	 * @param c the class to convert into
	 * @return the instance constructed
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InstantiationException the instantiation exception
	 * @throws NumberFormatException the number format exception
	 * @throws NullPointerException the null pointer exception
	 */
	protected static Object getFromJSON(JSONObject json, Class<?> c) throws IllegalAccessException,
			InstantiationException, NumberFormatException, NullPointerException {
		Field[] fields = null;
		fields = c.getDeclaredFields();
		Object obj = c.newInstance();

		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String name = f.getName();
			String JSONName = name.replace('_', '-');
			Type type = f.getType();
			String typeStr = type.toString();
			try {
				if (typeStr.equals(INT_TYPE)) {
					String value = json.getString(JSONName).toLowerCase();
					int iVal = 0;
					if(value.startsWith("#")){
						iVal = Color.WHITE;
						try{
							if(value.startsWith("#0x")){
								iVal = Integer.decode(value.substring(1)).intValue();
							}
							else{
								iVal = Integer.parseInt(value.substring(1), 16);
							}
						}catch (NumberFormatException e) {
						}
					}
					else{
						iVal = Integer.parseInt(value);
					}
					f.set(obj, iVal);
				} else if (typeStr.equals(STRING_TYPE)) {
					String value = json.getString(JSONName);
					f.set(obj, value);
				} else if (typeStr.equals(BOOLEAN_TYPE)) {
					boolean value = json.getBoolean(JSONName);
					f.set(obj, value);
				} else if (typeStr.equals(FLOAT_TYPE)) {
					float value = Float.parseFloat(json.getString(JSONName));
					f.set(obj, value);
				} else if (typeStr.equals(NAVIGATION_TYPE)) {
					NavigationStringEnum value = NavigationStringEnum.fromString(json.getString(JSONName));
					f.set(obj, value);
				} else if (typeStr.equals(TRANSITION_TYPE)) {
					TransitionStringEnum value = TransitionStringEnum.fromString(json.getString(JSONName));
					f.set(obj, value);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		return obj;
	}

	/**
	 * The Class ReflectedParcelable.
	 */
	public static class ReflectedParcelable implements Parcelable {

		/**
		 * Instantiates a new reflected parcelable.
		 */
		public ReflectedParcelable() {

		}

		/* (non-Javadoc)
		 * @see android.os.Parcelable#describeContents()
		 */
		@Override
		public int describeContents() {
			return 0;
		}

		/**
		 * Instantiates a new reflected parcelable.
		 *
		 * @param in the in
		 */
		protected ReflectedParcelable(Parcel in) {
			Field[] fields = null;
			Class<?> c = this.getClass();
			fields = c.getDeclaredFields();
			try {
				Object obj = this;
				for (int i = 0; i < fields.length; i++) {
					Field f = fields[i];
					
					Class<?> type = f.getType();
					
					if (type.isEnum()) {
						String typeStr = type.toString();
						if (typeStr.equals(NAVIGATION_TYPE)) {
							f.set(obj, NavigationStringEnum.fromString(in.readString()));
						} else if (typeStr.equals(TRANSITION_TYPE)) {
							f.set(obj, TransitionStringEnum.fromString(in.readString()));
						}
					} else {
						Object dt = f.get(this);
						if( !(dt instanceof Parcelable.Creator<?>)) {
							f.set(obj, in.readValue(null));							
						}
					}
				}
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		/* (non-Javadoc)
		 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
		 */
		@Override
		public void writeToParcel(Parcel out, int flags1) {
			Field[] fields = null;
			Class<?> c = this.getClass();
			fields = c.getDeclaredFields();
			try {
				for (int i = 0; i < fields.length; i++) {
					Field f = fields[i];
					Class<?> type = f.getType();
										
					if (type.isEnum()) {
						String typeStr = type.toString();
						if (typeStr.equals(NAVIGATION_TYPE)) {
							out.writeString(((NavigationStringEnum) f.get(this)).getText());
						} else if (typeStr.equals(TRANSITION_TYPE)) {
							out.writeString(((TransitionStringEnum) f.get(this)).getText());
						}
					} else {
						Object dt = f.get(this);
						if( !(dt instanceof Parcelable.Creator<?>)) 
								out.writeValue(dt);				
						
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Stop all listeners.
	 */
	public abstract void stopAllListeners();

}
