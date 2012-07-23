package nz.gen.geek_central.GLUseful;
/*
    Useful OpenGL-ES-2.0-related definitions.

    Copyright 2012 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
*/

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;

public class GLUseful
  {
    public static final android.opengl.GLES20 gl = new android.opengl.GLES20(); /* for easier references */
    public static final java.util.Locale StdLocale = java.util.Locale.US; /* for when I don't actually want a locale */

    static
      {
        System.loadLibrary("gl_useful");
      } /*static*/

/*
    Error checking
*/

    public static void ThrowError
      (
        int Err,
        String DoingWhat
      )
      {
        throw new RuntimeException
          (
            String.format
              (
                StdLocale,
                "OpenGL error %d %s",
                Err,
                DoingWhat
              )
          );
      } /*ThrowError*/

    public static void ThrowError
      (
        String DoingWhat
      )
      {
        ThrowError(gl.glGetError(), DoingWhat);
      } /*ThrowError*/

    public static void ThrowError
      (
        String DoingWhat,
        Object... FmtArgs
      )
      {
        ThrowError(String.format(StdLocale, DoingWhat, FmtArgs));
      } /*ThrowError*/

    public static void CheckError
      (
        String DoingWhat
      )
      {
        int Err = gl.glGetError();
        if (Err != 0)
          {
            ThrowError(Err, DoingWhat);
          } /*if*/
      } /*CheckError*/

    public static void CheckError
      (
        String DoingWhat,
        Object... FmtArgs
      )
      {
        CheckError(String.format(StdLocale, DoingWhat, FmtArgs));
      } /*CheckError*/

    public static native String GetShaderInfoLog
      (
        int ShaderID
      );
      /* needed because android.opengl.GLES20.glGetShaderInfoLog doesn't return anything,
        at least on Android 2.2. */

/*
    Utility types
*/

    public static class Color
      /* RGB colours with transparency */
      {
        public final float r, g, b, a;

        public Color
          (
            float r,
            float g,
            float b,
            float a
          )
          {
            this.r = r;
            this.b = b;
            this.g = g;
            this.a = a;
          } /*Color*/

      } /*Color*/;

/*
    Vertex arrays

    Need to use allocateDirect to allocate buffers so garbage
    collector won't move them. Also make sure byte order is
    always native. But direct-allocation and order-setting methods
    are only available for ByteBuffer. Which is why buffers
    are allocated as ByteBuffers and then converted to more
    appropriate types.
*/

    public static final int Fixed1 = 0x10000; /* for converting between float & fixed values */

    public static class FixedVec3Buffer
      /* converts an ArrayList of vectors to a vertex-attribute buffer. */
      {
        private final IntBuffer Buf;

        public FixedVec3Buffer
          (
            ArrayList<Vec3f> FromArray
          )
          {
            final int[] Vals = new int[FromArray.size() * 3];
            int jv = 0;
            for (int i = 0; i < FromArray.size(); ++i)
              {
                final Vec3f Vec = FromArray.get(i);
                Vals[jv++] = (int)(Vec.x * Fixed1);
                Vals[jv++] = (int)(Vec.y * Fixed1);
                Vals[jv++] = (int)(Vec.z * Fixed1);
              } /*for*/
            Buf =
                ByteBuffer.allocateDirect(Vals.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(Vals);
            Buf.position(0);
          } /*FixedVec3Buffer*/

        public void Apply
          (
            int AttribLoc,
            boolean Enable
          )
          {
            if (Enable)
              {
                gl.glEnableVertexAttribArray(AttribLoc);
              } /*if*/
            gl.glVertexAttribPointer(AttribLoc, 3, gl.GL_FIXED, true, 0, Buf);
          } /*Apply*/

      } /*FixedVec3Buffer*/;

    public static class ByteColorBuffer
      /* converts an ArrayList of colours to a vertex-attribute buffer. */
      {
        private final ByteBuffer Buf;

        public ByteColorBuffer
          (
            ArrayList<Color> FromArray
          )
          {
            final byte[] Vals = new byte[FromArray.size() * 4];
            int jv = 0;
            for (int i = 0; i < FromArray.size(); ++i)
              {
                final Color Val = FromArray.get(i);
                Vals[jv++] = (byte)(Val.r * 255);
                Vals[jv++] = (byte)(Val.g * 255);
                Vals[jv++] = (byte)(Val.b * 255);
                Vals[jv++] = (byte)(Val.a * 255);
              } /*for*/
            Buf =
                ByteBuffer.allocateDirect(Vals.length)
                .order(ByteOrder.nativeOrder())
                .put(Vals);
            Buf.position(0);
          } /*ByteColorBuffer*/

        public void Apply
          (
            int AttribLoc,
            boolean Enable
          )
          {
            if (Enable)
              {
                gl.glEnableVertexAttribArray(AttribLoc);
              } /*if*/
            gl.glVertexAttribPointer(AttribLoc, 4, gl.GL_UNSIGNED_BYTE, true, 0, Buf);
          } /*Apply*/
      } /*ByteColorBuffer*/;

    public static class VertIndexBuffer
      /* converts an ArrayList of vertex indices to a buffer that can be passed to glDrawElements. */
      {
        private final ShortBuffer Buf;
        private final int Mode;

        public VertIndexBuffer
          (
            ArrayList<Integer> FromArray,
            int Mode
          )
          {
            this.Mode = Mode;
            final short[] Indices = new short[FromArray.size()];
            for (int i = 0; i < Indices.length; ++i)
              {
                Indices[i] = (short)(int)FromArray.get(i);
              } /*for*/
            Buf =
                ByteBuffer.allocateDirect(Indices.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(Indices);
            Buf.position(0);
          } /*VertIndexBuffer*/

        public void Draw()
          {
            gl.glDrawElements
              (
                /*mode =*/ Mode,
                /*count =*/ Buf.limit(),
                /*type =*/ gl.GL_UNSIGNED_SHORT,
                /*indices =*/ Buf
              );
          } /*Draw*/

      } /*VertIndexBuffer*/;

/*
    Shader programs

    Unfortunately, the error checks marked "spurious failures!" can
    return mysterious failures after repeated shader creation.
    Ignoring the errors seems to work fine.
*/

    public static class Shader
      {
        public final int id;

        public Shader
          (
            int Type,
            String Source
          )
          {
            id = gl.glCreateShader(Type);
            if (id == 0)
              {
                ThrowError("creating shader");
              } /*if*/
            gl.glShaderSource(id, Source);
          /* CheckError("setting shader %d source", id); */ /* spurious failures! */
            gl.glCompileShader(id);
          /* CheckError("compiling shader %d source", id); */ /* spurious failures! */
            int[] Status = new int[1];
            gl.glGetShaderiv(id, gl.GL_COMPILE_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                System.err.println
                  (
                        "GLUseful failed to compile shader source:\n"
                    +
                        Source
                    +
                        "\n"
                  ); /* debug */
                throw new RuntimeException
                  (
                        "Error compiling shader: "
                    +
                        GetShaderInfoLog(id)
                  );
              } /*if*/
          } /*Shader*/

        public void Release()
          {
            gl.glDeleteShader(id);
          } /*Release*/

      } /*Shader*/

    public static class Program
      {
        public final int id;
        private final Shader VertexShader, FragmentShader;
        private final boolean OwnShaders;

        public Program
          (
            Shader VertexShader,
            Shader FragmentShader,
            boolean OwnShaders /* call Release on shaders on my own Release */
          )
          {
            id = gl.glCreateProgram();
            if (id == 0)
              {
                ThrowError("creating program");
              } /*if*/
            this.VertexShader = VertexShader;
            this.FragmentShader = FragmentShader;
            this.OwnShaders = OwnShaders;
            gl.glAttachShader(id, VertexShader.id);
          /* CheckError("attaching vertex shader to program %d", id); */ /* spurious failures! */
            gl.glAttachShader(id, FragmentShader.id);
          /* CheckError("attaching fragment shader to program %d", id); */ /* spurious failures! */
            gl.glLinkProgram(id);
            int[] Status = new int[1];
            gl.glGetProgramiv(id, gl.GL_LINK_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                throw new RuntimeException
                  (
                        "Error linking program: "
                    +
                        gl.glGetProgramInfoLog(id)
                  );
              } /*if*/
          } /*Program*/

        public Program
          (
            String VertexShaderSource,
            String FragmentShaderSource
          )
          {
            this
              (
                new Shader(gl.GL_VERTEX_SHADER, VertexShaderSource),
                new Shader(gl.GL_FRAGMENT_SHADER, FragmentShaderSource),
                true
              );
          } /*Program*/

        public void Validate()
          {
            gl.glValidateProgram(id);
            int[] Status = new int[1];
            gl.glGetProgramiv(id, gl.GL_VALIDATE_STATUS, Status, 0);
            if (Status[0] == gl.GL_FALSE)
              {
                throw new RuntimeException
                  (
                        "Error validating program: "
                    +
                        gl.glGetProgramInfoLog(id)
                  );
              } /*if*/
          } /*Validate*/

        public int GetUniform
          (
            String Name,
            boolean MustExist
          )
          {
            final int Result = gl.glGetUniformLocation(id, Name);
            if (MustExist && Result < 0)
              {
                throw new RuntimeException("no location for uniform “" + Name + "”");
              } /*if*/
            return Result;
          } /*GetUniform*/

        public int GetAttrib
          (
            String Name,
            boolean MustExist
          )
          {
            final int Result = gl.glGetAttribLocation(id, Name);
            if (MustExist && Result < 0)
              {
                throw new RuntimeException("no location for attribute “" + Name + "”");
              } /*if*/
            return Result;
          } /*GetAttrib*/

        public void Use()
          {
            gl.glUseProgram(id);
          } /*Use*/

        public void Unuse()
          {
            gl.glUseProgram(0);
          } /*Unuse*/

        public void Release()
          {
            gl.glDetachShader(id, VertexShader.id);
            gl.glDetachShader(id, FragmentShader.id);
            if (OwnShaders)
              {
                VertexShader.Release();
                FragmentShader.Release();
              } /*if*/
            gl.glDeleteProgram(id);
          } /*Release*/

      } /*Program*/

  } /*GLUseful*/
