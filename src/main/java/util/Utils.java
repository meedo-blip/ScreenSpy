package util;

import com.sun.tools.javac.Main;
import jade.Transform;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Utils {
	public static final float DEGREES_TO_RADIANS = 0.017453292f;


	public static byte[] intsToBytes(int[] ints, byte[] bytes) {

		for (int i = 0; i < ints.length; i++) {
			bytes[i * 4]     = (byte) ((ints[i] >> 16) & 0xFF); // R
			bytes[i * 4 + 1] = (byte) ((ints[i] >> 8) & 0xFF);  // G
			bytes[i * 4 + 2] = (byte) (ints[i] & 0xFF);         // B
			bytes[i * 4 + 3] = (byte) ((ints[i] >> 24) & 0xFF); // A
		}
		return bytes;
	}

	public static int[] findWord(String myWord, String text) {
		return findWord(myWord, text, 0);
	}

	public static int[] findWord(String myWord, String text, int offset) {
		int[] pos = null;

		char[] wordArray = myWord.toCharArray();
		char[] textArray = text.toCharArray();

		final int iterations = textArray.length - wordArray.length + 1;

		for (int i = 0; i < iterations; i++) {
			int count = 0;

			for (int j = 0; j < wordArray.length; j++) {
				if (textArray[i + j] == wordArray[j]) count++;
				else break;
			}

			if (count == wordArray.length) {
				if (pos == null)
					pos = new int[1];
				else {
					int[] temp = pos;
					pos = new int[pos.length + 1];

					for (int m = 0; m < temp.length; m++)
						pos[m] = temp[m];
				}
				pos[pos.length - 1] = i + offset;

				i += count - 1;
			}
		}

		return pos;
	}

	public static int generateTexture(ByteBuffer image, int width, int height, int channels) {

		// Generate texture on GPU
		int texID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texID);

		// For smoother scaling:
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Set texture parameter
		// Repeat image in both directions
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		// When stretching image, pixelate
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		// When shrinking an image, also pixelate
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		if (image != null) {
			// Upload image and its format as a texture to OpenGL
			if (channels == 4) {
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
						0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			} else if (channels == 3) {

				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height,
						0, GL_RGB, GL_UNSIGNED_BYTE, image);

				int error = glGetError();
				if (error != GL_NO_ERROR) {
					System.out.println("OpenGL Error: " + error);
				}
			} else {
				assert false : "Error: (Texture) Unknown number of channels '" + channels + "'";
			}
		} else {
			assert false : "Error: (Texture) Could not load image with id '" + texID + "'";
		}


		return texID;
	}

	public static int createTexture(String filepath, boolean flip_on_load) {

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);

		// Images will be flipped back up from upside down
		stbi_set_flip_vertically_on_load(flip_on_load);
		ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

		int texID = generateTexture(image, width.get(0), height.get(0), channels.get(0));

		// free up the image from memory
		if (image != null)
			stbi_image_free(image);

		return texID;
	}

	public static Vector4f hexToRgba(String hex) {
		hex = hex.trim();
		if (hex.charAt(0) == '#') {
			hex = hex.substring(1, 7);
		}
		float r = (float) Integer.valueOf(hex.substring(0, 2), 16) / 255;
		float g = (float) Integer.valueOf(hex.substring(2, 4), 16) / 255;
		float b = (float) Integer.valueOf(hex.substring(4, 6), 16) / 255;

		System.out.println(r + " " + g + " " + b);
		return new Vector4f(r, g, b, 1f);
	}

	public static ByteBuffer bitmapToRgba(byte[] array, int channels) {
		int size = (array.length * 4) / channels;
		byte[] array1 = new byte[size];

		for (int i = 0; i < size; i++) {
			int pos = (i * channels) / 4;
			if (pos < array.length)
				array1[i] = array[pos];
		}

		return BufferUtils.createByteBuffer(size).put(array1);
	}

	public static float[] toFloatArray(List<Double> list){
		float[] arr = new float[list.size()];
		for(int i = 0; i < arr.length; i++)
			arr[i] = (float)(double) list.get(i);

		return arr;
	}

	public static int getRandomInteger(int a, int b) {
		return (int) (Math.random() * (a + b)) - a;
	}

	public static boolean checkCollision(Transform transform1, Transform transform2) {

		// 00 01 10 11
		float ax1 = transform1.position.x - (transform1.scale.x / 2);
		float ay1 = transform1.position.y + (transform1.scale.y / 2);

		float ax2 = transform1.position.x + (transform1.scale.x / 2);
		float ay2 = transform1.position.y + (transform1.scale.y / 2);

		float ax3 = transform1.position.x - (transform1.scale.x / 2);
		float ay3 = transform1.position.y - (transform1.scale.y / 2);

		float ax4 = transform1.position.x + (transform1.scale.x / 2);
		float ay4 = transform1.position.y - (transform1.scale.y / 2);

		// 00 11
		float bx1 = transform2.position.x - (transform2.scale.x / 2);
		float by1 = transform2.position.y + (transform2.scale.y / 2);

		float bx2 = transform2.position.x + (transform2.scale.x / 2);
		float by2 = transform2.position.y - (transform2.scale.y / 2);

		if(ax1 >= bx1 && ax1 <= bx2 && ay1 <= by1 && ay1 >= by2) return true;
		if(ax2 >= bx1 && ax2 <= bx2 && ay2 <= by1 && ay2 >= by2) return true;
		if(ax3 >= bx1 && ax3 <= bx2 && ay3 <= by1 && ay3 >= by2) return true;
        return ax4 >= bx1 && ax4 <= bx2 && ay4 <= by1 && ay4 >= by2;
    }

	public static synchronized void playSound(final String url) {
		new Thread(new Runnable() {
			// The wrapper thread is unnecessary, unless it blocks on the
			// Clip finishing; see comments.
			public void run() {
				try {
					Clip clip = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(
							Main.class.getResourceAsStream(url));
					clip.open(inputStream);
					clip.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}

	public static void updateTexture(int textureId, ByteBuffer bytes, int texWidth, int texHeight, int channels) {
		glBindTexture(GL_TEXTURE_2D, textureId);

		if (channels == 4) {
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, texWidth, texHeight,
					GL_RGBA, GL_UNSIGNED_BYTE, bytes);
		} else if (channels == 3) {
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, texWidth, texHeight,
					GL_RGB, GL_UNSIGNED_BYTE, bytes);
		} else {
			assert false : "Error: (Texture) Unknown number of channels '" + channels + "'";
		}
	}
}

