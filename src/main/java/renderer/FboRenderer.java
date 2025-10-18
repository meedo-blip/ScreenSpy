package renderer;

import jade.Window;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

public class FboRenderer {

    private int fbo;
    private int textureColorBuffer;
    private int rboDepthStencil;
    private int width, height;

    public FboRenderer() {
        width = Window.getWidth();
        height = Window.getHeight();
        initFBO();
    }

    private void initFBO() {
        // Create the FBO
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        // Create a color attachment texture
        textureColorBuffer = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureColorBuffer);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureColorBuffer, 0);

        // Create a depth and stencil renderbuffer object
        rboDepthStencil = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepthStencil);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rboDepthStencil);

        // Check if FBO is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer is not complete!");
        }

        // Unbind the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glViewport(0, 0, width, height);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getTexture() {
        return textureColorBuffer;
    }

    public void destroy() {
        glDeleteFramebuffers(fbo);
        glDeleteTextures(textureColorBuffer);
        glDeleteRenderbuffers(rboDepthStencil);
    }
}

