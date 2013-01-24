package com.emediate.controller.util;

public enum TransitionStringEnum {

	DEFAULT("default"), DISSOLVE("dissolve"), FADE("fade"), ROLL("roll"), SLIDE("slide"), ZOOM("zoom"), NONE("none");

	private String text;

	TransitionStringEnum(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static TransitionStringEnum fromString(String text) {
		if (text != null) {
			for (TransitionStringEnum b : TransitionStringEnum.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}
}
