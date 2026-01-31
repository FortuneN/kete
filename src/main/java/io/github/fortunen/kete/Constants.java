package io.github.fortunen.kete;

public final class Constants {

	private Constants() {}

	public static final String ID = "kete";
	public static final String KIND = "kind";
	public static final String EVENT = "EVENT";
	public static final String ERROR = "ERROR";
	public static final String SUCCESS = "SUCCESS";
	public static final String ADMIN_EVENT = "ADMIN_EVENT";
	public static final String MESSAGE_HEADER_EVENT_KIND = "eventkind";
	public static final String MESSAGE_HEADER_EVENT_TYPE = "eventtype";
	public static final String PACKAGE = Constants.class.getPackageName();
	public static final String MESSAGE_HEADER_CONTENT_TYPE = "contenttype";
	public static final String VERSION = Constants.class.getPackage().getImplementationVersion();
}
