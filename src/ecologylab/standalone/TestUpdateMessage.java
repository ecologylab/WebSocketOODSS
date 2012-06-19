package ecologylab.standalone;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.server.clientsessionmanager.SessionHandle;
import ecologylab.oodss.messages.UpdateMessage;
import ecologylab.serialization.annotations.simpl_scalar;


public class TestUpdateMessage extends UpdateMessage{
	  @simpl_scalar
	  private String  message;

	  @simpl_scalar
	  private String  tone;

	  @simpl_scalar
	  private int    points;

	  /**
	   * Constructor used on client. Fields populated automatically by
	   * s.im.pl serialization
	   */
	  public TestUpdateMessage()
	  {
	  }
	  
	  /**
	   * Constructor used on server
	   * 
	   * @param message
	   *           the chat message
	   * 
	   * @param handle
	   *           handle of originating client
	   */
	  public TestUpdateMessage(String message, String tone, int points)
	  {
	    this.message = message;
	    this.tone = tone;
	    this.points = points;
	  }

	  /**
	   * Called automatically by OODSS on client
	   */
	  @Override
	  public void processUpdate(Scope appObjScope)
	  {
	    /* get the chat listener */
	    TestUpdateMessageListener listener = (TestUpdateMessageListener) appObjScope
	        .get(TestUpdateMessageListener.TEST_UPDATE_LISTENER);

	    /* report incoming update */
	    listener.recievedUpdate(this);
	  }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTone() {
		return tone;
	}

	public void setTone(String tone) {
		this.tone = tone;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
}