/**
 * 
 */
package org.bdgp.somviewer.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author erwin
 *
 */
public abstract class AbstractBaseAsyncHandler<T> implements AsyncCallback<T>, AsyncHandler<T> {

	public AbstractBaseAsyncHandler()
	{
	}
	
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
	 */
	public void onFailure(Throwable caught) {
		// Call the derived class implementation
		handleFailure(caught);
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
	 */
	public void onSuccess(T result) {
		// Call the derived class implementation
		handleSuccess(result);
	}

	/* (non-Javadoc)
	 * @see org.gwtbook.client.AsyncHandler#handleFailure(java.lang.Throwable)
	 */
	public abstract void handleFailure(Throwable caught); 
	
	/* (non-Javadoc)
	 * @see org.gwtbook.client.AsyncHandler#handleSuccess(java.lang.Object)
	 */
	public abstract void handleSuccess(T result); 
}
