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
package dk.alexandra.fresco.lib.arithmetic;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;


/**
 * Generic test cases for basic finite field operations.
 * 
 * Can be reused by a test case for any protocol suite that implements the basic
 * field protocol factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in
 * mpc.lib or something.
 *
 */
public class BasicArithmeticTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestInput extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory prov = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(
									prov);
							SInt input1 = ioBuilder.input(
									BigInteger.valueOf(10), 1);

							OInt output = ioBuilder.output(input1);
							ProtocolProducer io = ioBuilder.getCircuit();

							ProtocolProducer gp = new SequentialProtocolProducer(
									io);
							this.outputs = new OInt[] { output };
							return gp;
						}
					};

					sce.runApplication(app);

					Assert.assertEquals(BigInteger.valueOf(10),
							app.getOutputs()[0].getValue());
				}
			};
		}
	}

	public static class TestCopyProtocol extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = -8310958118835789509L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory prov = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(
									prov);
							
							SInt closed = ioBuilder.input(BigInteger.ONE, 1);
							ProtocolProducer inp = ioBuilder.getCircuit();
							ioBuilder.reset();
							
							SInt into = prov.getSInt();
							ProtocolProducer copy = new CopyProtocolImpl<SInt>(closed, into);
							OInt open = ioBuilder.output(into);
							ProtocolProducer out = ioBuilder.getCircuit();
							this.outputs = new OInt[] {open};
							
							
							SequentialProtocolProducer seq = new SequentialProtocolProducer(inp, copy, out);
							return seq;
						}
					};
					sce.runApplication(app);

					Assert.assertEquals(app.getOutputs()[0].getValue(), BigInteger.ONE);
				}
			};
		}
	};
	
	public static class TestLotsOfInputs extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					final int[] openInputs = new int[] { 1, 2, 3, 4, 5, 6, 7,
							8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = -8310958118835789509L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory prov = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(
									prov);

							SInt[] inputs = createInputs(ioBuilder, openInputs,
									1);

							OInt[] outputs = ioBuilder.outputArray(inputs);
							this.outputs = outputs;
							ProtocolProducer io = ioBuilder.getCircuit();

							ProtocolProducer gp = new SequentialProtocolProducer(
									io);
							return gp;
						}
					};
					sce.runApplication(app);

					checkOutputs(openInputs, app.getOutputs());
				}
			};
		}
	};

	public static class TestSumAndMult extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					final int[] openInputs = new int[] { 1, 2, 3, 4, 5, 6, 7,
							8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = -8310958118835789509L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory prov = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(
									prov);

							SInt[] inputs = createInputs(ioBuilder, openInputs,
									1);

							ProtocolProducer inp = ioBuilder.getCircuit();
							ioBuilder.reset();

							// create wire
							SInt sum = prov.getSInt();

							// create Sequence of protocols which eventually
							// will compute the sum
							SequentialProtocolProducer sumProtocol = new SequentialProtocolProducer();

							sumProtocol.append(prov.getAddCircuit(inputs[0],
									inputs[1], sum));
							if (inputs.length > 2) {
								for (int i = 2; i < inputs.length; i++) {
									// Add sum and next secret shared input and
									// store in sum.
									sumProtocol.append(prov.getAddCircuit(sum,
											inputs[i], sum));
								}
							}

							sumProtocol.append(prov.getMultCircuit(sum, sum,
									sum));

							this.outputs = new OInt[] { ioBuilder.output(sum) };

							ProtocolProducer io = ioBuilder.getCircuit();

							ProtocolProducer gp = new SequentialProtocolProducer(
									inp, sumProtocol, io);
							return gp;
						}
					};
					sce.runApplication(app);
					int sum = 0;
					for (int i : openInputs) {
						sum += i;
					}
					sum = sum * sum;
					Assert.assertEquals(BigInteger.valueOf(sum),
							app.getOutputs()[0].getValue());
				}
			};
		};
	};

	private static void checkOutputs(int[] openInputs, OInt[] outputs) {
		for (int i = 0; i < openInputs.length; i++) {
			Assert.assertEquals(BigInteger.valueOf(openInputs[i]),
					outputs[i].getValue());
		}
	}

	private static SInt[] createInputs(NumericIOBuilder ioBuilder, int[] input,
			int targetID) {
		BigInteger[] bs = new BigInteger[input.length];
		int inx = 0;
		for (int i : input) {
			bs[inx] = BigInteger.valueOf(i);
			inx++;
		}
		return ioBuilder.inputArray(bs, targetID);
	}

	public static class TestSimpleMultAndAdd extends TestThreadFactory {

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 701623461111107585L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory provider) {
							BasicNumericFactory prov = (BasicNumericFactory) provider;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(
									prov);
							SInt input1 = ioBuilder.input(
									BigInteger.valueOf(10), 1);
							SInt input2 = ioBuilder.input(
									BigInteger.valueOf(5), 1);

							ProtocolProducer inputs = ioBuilder.getCircuit();
							ioBuilder.reset();
							NumericProtocolBuilder builder = new NumericProtocolBuilder(
									prov);
							SInt addAndMult = builder.mult(input1,
									builder.add(input1, input2));
							ProtocolProducer circ = builder.getCircuit();

							OInt output = ioBuilder.output(addAndMult);
							this.outputs = new OInt[] { output };
							ProtocolProducer outputs = ioBuilder.getCircuit();

							ProtocolProducer gp = new SequentialProtocolProducer(
									inputs, circ, outputs);
							return gp;
						}
					};
					sce.runApplication(app);

					Assert.assertEquals(BigInteger.valueOf(10 * (10 + 5)),
							app.getOutputs()[0].getValue());
				}
			};
		}
	}

}
