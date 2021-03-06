/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.add;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;

public class OneBitHalfAdderCircuitImpl implements OneBitHalfAdderCircuit{

	private SBool left, right, outS;
	private SBool outCarry;
	private BasicLogicFactory provider;
	private int round;
	private ParallelProtocolProducer curGP;
	
	public OneBitHalfAdderCircuitImpl(SBool left, SBool right, SBool outS,
			SBool outCarry, BasicLogicFactory provider) {
		this.left = left;
		this.right = right;
		this.outS = outS;
		this.outCarry = outCarry;
		this.provider = provider;
		this.round = 0;
		this.curGP = null;
	}

	@Override
	public int getNextProtocols(NativeProtocol[] gates, int pos) {
		if(round == 0){
			if(curGP == null){
				XorProtocol xor = provider.getXorCircuit(left, right, outS);
				AndProtocol and = provider.getAndCircuit(left, right, outCarry);
				curGP = new ParallelProtocolProducer(xor, and);
			}
			if(curGP.hasNextProtocols()){
				pos = curGP.getNextProtocols(gates, pos);
			}
			else if(!curGP.hasNextProtocols()){
				curGP = null;
				round++;
			}
		}
		return pos;
	}

	@Override
	public boolean hasNextProtocols() {
		return round < 1;
	}
	
	@Override
	public Value[] getInputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		// TODO Auto-generated method stub
		return null;
	}

}
