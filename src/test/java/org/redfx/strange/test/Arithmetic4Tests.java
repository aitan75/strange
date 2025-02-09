/*-
 * #%L
 * Strange
 * %%
 * Copyright (C) 2020 Johan Vos
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Johan Vos nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.redfx.strange.test;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.redfx.strange.Complex;
import org.redfx.strange.ControlledBlockGate;
import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
//import org.redfx.strange.gate.AddIntegerModulus;
import org.redfx.strange.gate.AddModulus;
import org.redfx.strange.gate.Hadamard;
import org.redfx.strange.gate.InvFourier;
import org.redfx.strange.gate.MulModulus;
import org.redfx.strange.gate.X;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;


/**
 *
 * @author johan
 */
public class Arithmetic4Tests extends BaseGateTests {

    static final double D = 0.000000001d;

   // @Test
//    public void addmodgate() {
//        int a = 3;
//        int n = 3;
//        int mod = 4;
//        int dim = n + 2;
//        Program p = new Program(dim);
//        Step prep = new Step();
//        prep.addGates(new X(1));
//        p.addStep(prep);
//        
//        Step modstep = new Step(new AddIntegerModulus(0,3,a, mod));
//        p.addStep(modstep);
//        Result result = runProgram(p);
//        Qubit[] q = result.getQubits();
//        for (int i = 0; i < q.length; i++) {
//            System.err.println("Qubit["+i+"] = "+q[i].measure());
//        }
//        assertEquals(dim, q.length);
//        assertEquals(1, q[0].measure());
//        assertEquals(0, q[1].measure());
//        assertEquals(0, q[2].measure());
//        assertEquals(0, q[3].measure());
//        assertEquals(0, q[4].measure());
//    }
//    
    
    @Test
    public void testexpmodHHHHnoinv() {
        // 0 H H H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        for (int i = 0; i < offset; i++) {
            prep.addGate(new Hadamard(i));
        }
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
       // for (int i = length - 1; i > length - 1 - offset; i--) {
        for (int i = length - 1; i > length - 2; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            if (probs[i].abssqr() > 0.000001) 
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            assertEquals(amps[i], 1./16, 0.001);
        }
        Qubit[] qubits = result.getQubits();
    }
    
    
  //  @Test
    public void testexpmodHHHH() {
        // 0 H H H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        for (int i = 0; i < offset; i++) {
            prep.addGate(new Hadamard(i));
        }
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > length - 1 - offset; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            if (i%4 == 0) {
                assertEquals(amps[i], .25, 0.001);
            } else {
                assertEquals(amps[i], 0, 0.001);
            }
        }
    }
    
   // @Test
    public void testexpmod0HHH() {
        // 0 H H H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        for (int i = 1; i < offset; i++) {
            prep.addGate(new Hadamard(i));
        }
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > length - 1 - offset; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            if (i%4 == 0) {
                assertEquals(amps[i], .25, 0.001);
            } else {
                assertEquals(amps[i], 0, 0.001);
            }
        }
    }
    
   // @Test
    public void testexpmod00HH() {
        // 0 0 H H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        for (int i = 2; i < offset; i++) {
            prep.addGate(new Hadamard(i));
        }
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > length - 1 - offset; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            if (i%4 == 0) {
                assertEquals(amps[i], .25, 0.001);
            } else {
                assertEquals(amps[i], 0, 0.001);
            }
        }
    }
    
    //@Test
    public void testexpmod1() {
        // 1 0 0 0 { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        prep.addGates(new X(0));
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = length - 1; i > length - 1 - offset; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            assertEquals(amps[i], 1./16, 0.001);
        }
        
    }

   // @Test
    public void testexpmodH() {
        // H 0 0 0 { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;

        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();

        prep.addGate(new Hadamard(0));
        
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = offset - 1; i > - 1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            System.err.println("Create MulModulus, i = "+i+", m = "+m+", a = "+a+", MOD = "+mod);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            System.err.println("AMP["+i+"] = "+amps[i]);
            assertEquals(amps[i], 1./16, 0.001);
        }
    }
    
   // @Test
    public void testexpmod000H() {
        // 0 0 0 H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length;

        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();

        prep.addGate(new Hadamard(3));
        
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = offset - 1; i > - 1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            System.err.println("Create MulModulus, i = "+i+", m = "+m+", a = "+a+", MOD = "+mod);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[16];
        for (int i = 0; i < probs.length; i++) {
            amps[i%16] = amps[i%16] + probs[i].abssqr();
        }
        for (int i = 0; i < 16; i ++) {
            System.err.println("AMP["+i+"] = "+amps[i]);
            if (i%2 == 0) {
                assertEquals(amps[i], .125, 0.001);
            } else {
                assertEquals(amps[i], 0, 0.001);
            }
        }
    }

  //  @Test
    public void testexpmod00H() {
        // 0 0  H { 1 x 7 ^ A mod 15}
        int mod = 15;
        int a = 7;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = length -1 ;
        int dim0 = 1 << offset;
        assertEquals(dim0, 8);
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();

        prep.addGate(new Hadamard(2));
        
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = offset - 1; i > - 1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            System.err.println("Create MulModulus, i = "+i+", m = "+m+", a = "+a+", MOD = "+mod);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[dim0];
        for (int i = 0; i < probs.length; i++) {
            amps[i%dim0] = amps[i%dim0] + probs[i].abssqr();
        }
        for (int i = 0; i < dim0; i ++) {
            System.err.println("AMP["+i+"] = "+amps[i]);
            if (i%2 == 0) {
                assertEquals(amps[i], .25, 0.001);
            } else {
                assertEquals(amps[i], 0, 0.001);
            }
        }
    }
  
   // @Test
    public void testexpmodH37() {
        //  H { 1 x 3 ^ A mod 7}
        int mod = 7;
        int a = 3;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = 1 ;
        int dim0 = 1 << offset;
        assertEquals(dim0, 2);
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();

        prep.addGate(new X(3));
        
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = offset - 1; i > - 1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            System.err.println("Create MulModulus, i = "+i+", m = "+m+", a = "+a+", MOD = "+mod);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[dim0];
        for (int i = 0; i < probs.length; i++) {
            amps[i%dim0] = amps[i%dim0] + probs[i].abssqr();
        }
        for (int i = 0; i < dim0; i ++) {
            System.err.println("AMP["+i+"] = "+amps[i]);
            assertEquals(amps[i], .5, 0.001);
        }
    }
    
   // @Test
    public void testexpmodHH37() {
        //  H { 1 x 3 ^ A mod 7}
        int mod = 7;
        int a = 3;
        int length = (int) Math.ceil(Math.log(mod) / Math.log(2));
        int offset = 2 ;
        int dim0 = 1 << offset;
        assertEquals(dim0, 4);
        Program p = new Program(2 * length + 3 + offset);
        Step prep = new Step();
        for (int i = 0; i < offset; i++) {
            prep.addGate(new Hadamard(i));
        }
        
        Step prepAnc = new Step(new X(length + 1 + offset));
        p.addStep(prep);
        p.addStep(prepAnc);
        for (int i = offset - 1; i > - 1; i--) {
            int m = 1;
            for (int j = 0; j < 1 << i; j++) {
                m = m * a % mod;
            }
            System.err.println("Create MulModulus, i = "+i+", m = "+m+", a = "+a+", MOD = "+mod);
            MulModulus mul = new MulModulus(length, 2 * length, m, mod);
            ControlledBlockGate cbg = new ControlledBlockGate(mul, offset, i);
            p.addStep(new Step(cbg));
        }
        p.addStep(new Step(new InvFourier(offset, 0)));
        Result result = runProgram(p);
        Complex[] probs = result.getProbability();
        Qubit[] q = result.getQubits();
        int answer = 0;
        double[] amps = new double[dim0];
        for (int i = 0; i < probs.length; i++) {
            amps[i%dim0] = amps[i%dim0] + probs[i].abssqr();
        }
        for (int i = 0; i < dim0; i ++) {
         //   System.err.println("AMP["+i+"] = "+amps[i]);
            assertEquals(amps[i], .25, 0.001);
        }
    }
    
  //  @Test
    void testMinOffset() {
        int offset = 2;
        int n = 2;
        int dim = 2 * n + offset + 3;
        int mod = 3;
        Program p = new Program(dim);
        Step prep = new Step();
        for (int i = 0; i <offset; i++) {
            prep.addGate(new X(i));
        }
        prep.addGates(new X(2), new X(5));
        p.addStep(prep);
        AddModulus a1 = new AddModulus(offset,n+ offset,n+offset+1, offset+ 2*n+1, mod).inverse();
        p.addStep(new Step(a1));
        Result result = runProgram(p);
        Qubit[] q = result.getQubits();
        assertEquals(q[0].measure(), 1);
        assertEquals(q[1].measure(), 1);
        assertEquals(q[2].measure(), 0);
        assertEquals(q[3].measure(), 0);
        assertEquals(q[4].measure(), 0);
        assertEquals(q[5].measure(), 1);
        assertEquals(q[6].measure(), 0);
        assertEquals(q[7].measure(), 0);
        assertEquals(q[8].measure(), 0);
    }

}
