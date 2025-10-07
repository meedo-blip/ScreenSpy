package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20C.glUniform1iv;

public class Shader {

    private int shaderProgramID;
    private boolean beingUsed = false;

    private String vertexSource, fragmentSource;
    private String filepath;
    private String[] splitString;

    // filename without extension
    public Shader( String filepath) {
        this.filepath = new File(filepath).getAbsolutePath();

        String source = null;
        try{
            source = new String(Files.readAllBytes(Paths.get(this.filepath)));
        } catch (IOException e) {
            e.printStackTrace();
            assert false: "Error: An error was found in shader file: '" + filepath + "'";
        }

        splitString = source.split("(#type)( )+([a-zA-Z]+)");

        // Find all positions of #type
        int[] shaderTypePos = Utils.findWord("#type", source, 6);


        // Store the shader type at each position
        String[] shaderTypes = new String[shaderTypePos.length];

        for(int i = 0; i < shaderTypePos.length; i++) {
            shaderTypes[i] = source.substring(shaderTypePos[i], source.indexOf("\n", shaderTypePos[i])).trim();

            if(shaderTypes[i].equals("vertex")) {
                vertexSource = splitString[i+1];
            } else if(shaderTypes[i].equals("fragment")) {
                fragmentSource = splitString[i+1];
            } else {
                assert false : "Unexpected token '" + shaderTypes[i] + "'";
            }
        }
    }

    public void compile() {
        // =============================================================
        // Compile and link the shaders
        // =============================================================

        int vertexID, fragmentID;

        // Load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);

        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // error check
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: '" + filepath + "' \n\t compilation of vertex shader failed");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // Load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // Link the shaders and check for errors
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // error check
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+ filepath +"' \n\t compilation of fragment shader failed");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        System.out.println("2");

        // Check for linking errors
        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+ filepath +"' \n\t linking shaders failed");
            System.out.println(glGetProgramInfoLog(shaderProgramID, len));
            assert false : "";
        }
    }

    public void use() {
            // Bind shader program
            glUseProgram(shaderProgramID);	
    }

    public void detach() {
    	glUseProgram(0); // Use no shader program
    	beingUsed = false;
    }
    
    public void uploadMat4f(String varName, Matrix4f mat4) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName); // Find the uniform variable in the shader program
    	FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);        // Make 4 by 4 Matrix buffer
    	
    	use(); // Use shader
    	
    	mat4.get(matBuffer);											  // Write matrix data to buffer			
    	glUniformMatrix4fv(varLocation, false, matBuffer);                // Upload Matrix to uniform variable
    }
    
    public void uploadMat3f(String varName, Matrix3f mat3) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);         // Make 3 by 3 Matrix buffer
    	
    	use(); // Use shader
    	
    	mat3.get(matBuffer);											  			
    	glUniformMatrix3fv(varLocation, false, matBuffer);                // 3 vectors of 3 floats
    }
    
    public void uploadMat2f(String varName, Matrix2f mat2) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	FloatBuffer matBuffer = BufferUtils.createFloatBuffer(4);         // Make 2 by 2 Matrix buffer
    	
    	use(); // Use shader
    	
    	mat2.get(matBuffer);											  			
    	glUniformMatrix2fv(varLocation, false, matBuffer);                // 2 vectors of 2 floats               
    }
    
    public void uploadVec4f(String varName, Vector4f vec) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w); // 4 floats
    } 
    
    public void uploadVec3f(String varName, Vector3f vec) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform3f(varLocation, vec.x, vec.y, vec.z); // 3 floats
    }
    
    public void uploadVec2f(String varName, Vector2f vec) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform2f(varLocation, vec.x, vec.y); // 2 floats
    }

    public void uploadVec4i(String varName, Vector4i vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform4i(varLocation, vec.x, vec.y, vec.z, vec.w); // 4 integers
    }

    public void uploadVec3i(String varName, Vector3i vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform3i(varLocation, vec.x, vec.y, vec.z); // 3 integers
    }

    public void uploadVec2i(String varName, Vector2i vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform2i(varLocation, vec.x, vec.y); // 2 integers
    }

    public void uploadFloat(String varName, float val) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform1f(varLocation, val); // 1 float
    }
    
    public void uploadInt(String varName, int val) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform1i(varLocation, val); // 1 integer
    }
    
    public void uploadTexture(String varName, int slot) {
    	int varLocation = glGetUniformLocation(shaderProgramID, varName);
    	use();
    	glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1iv(varLocation, array);
    }

    public void uploadFloatArray(String varName, float[] arr) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1fv(varLocation, arr);
    }

    public Integer getId() {
        return shaderProgramID;
    }
}
