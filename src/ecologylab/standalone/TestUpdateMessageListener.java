package ecologylab.standalone;

public interface TestUpdateMessageListener {
	  public final static String TEST_UPDATE_LISTENER = "TEST_UPDATE_LISTENER";
	  void recievedUpdate(TestUpdateMessage response);
}