package jade;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
	private Matrix4f projectionMatrix, viewMatrix;
	public Vector2f position;

	private final float projection_h = 56f * 40f, projection_w = projection_h * 9f / 16f;
	
	public Camera(Vector2f position) {
		// Set vector position of camera
		this.position = position;
		
		// Make 4 by 4 projection Matrix
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		
		adjustProjection();
	}
	
	// Adjust projection resolution to window size
	public void adjustProjection() {
		projectionMatrix.identity();
		
		// Orthographic projection                                                                          Only render objects within zNear and zFar
		//                       v left   v set right in proj pixel                                           v zNear  v zFar
		projectionMatrix.ortho( 0,projection_h, projection_w,0, 0.0f, 100.0f);
		//                                                              ^ bottom               ^ set top in proj pixels
	}
	
	public Matrix4f getViewMatrix() { 
		Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f); // z position
		Vector3f cameraUp = new Vector3f(0.0f, 2.0f, 0.0f); // y position

		viewMatrix.identity();
		viewMatrix = viewMatrix.lookAt(new Vector3f(position.x, position.y, 0.0f), // Camera location
									   cameraFront.add(position.x, position.y, 0.0f), // Position that camera is looking towards
									   cameraUp);
		
		return viewMatrix;
	}

	public float getProjection_h() {
		return projection_h;
	}

	public float getProjection_w() {
		return projection_w;
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}


}
