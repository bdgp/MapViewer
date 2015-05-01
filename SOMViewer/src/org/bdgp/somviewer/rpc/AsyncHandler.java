/**
 * 
 */
package org.bdgp.somviewer.rpc;

/**
 * @author erwin
 *	Interface to deal with GWT onSuccess/onFailure calls based on logging abstract class
 */
public interface AsyncHandler<T> {
	void handleSuccess(T result);
	void handleFailure(Throwable caught);
}
