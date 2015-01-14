/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.game;

/**
 *
 * @author LostMekka
 */
public interface LoadingProgressListener {
	
	public void onProgress(float progress);
	public void onLoadingMessage(String message);
	public void onDone();
	
}
