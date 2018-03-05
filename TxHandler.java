import java.lang.reflect.Array;
import java.util.*;

public class TxHandler {

    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);


        System.out.println("\n\n");
        // All unspent transaction outputs
        ArrayList<UTXO> utxo = this.utxoPool.getAllUTXO();
        System.out.println("Unspent Output Pool Size: " + utxo.size());

        System.out.println("\n\nUnspent Outputs");
        System.out.println("-----------------------");
        // loop through unspent tx outputs
        for (UTXO txoutput : utxo) {
            Transaction.Output output = this.utxoPool.getTxOutput(txoutput);
            System.out.println("public address: " + output.address.getEncoded());
            System.out.println("value: " + output.value);
        }

    }

    /**
     * @return true if:
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // Transaction inputs and outputs
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();

        // Output/Input map
        HashMap<Integer,byte[]> opHashes;
        opHashes = new HashMap<Integer, byte[]>();

        // total inputs and outputs
        double totalInput = 0.0;
        double totalOutput = 0.0;

        int inputSize = inputs.size();
        for (int i = 0; i < inputSize; i ++) {
            System.out.println("\ninput " + i);
            System.out.println("---------------------------------");
            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output ut = this.utxoPool.getTxOutput(unspent);

            // (2) the signatures on each input of {@code tx} are valid,
            System.out.println("verified: " +
                    Crypto.verifySignature(ut.address, tx.getRawDataToSign(inputs.get(i).outputIndex), inputs.get(i).signature));

            totalInput += ut.value;

            opHashes.put(inputs.get(i).outputIndex, inputs.get(i).prevTxHash);
        }

        int outputSize = outputs.size();
        for (int i = 0; i < outputSize; i ++) {
            System.out.println("\noutput " + i);
            System.out.println("---------------------------------");

            // (4) all of {@code tx}s output values are non-negative, and
            System.out.println("output value non-negative: " + (outputs.get(i).value > 0));

            totalOutput += outputs.get(i).value;

            // (1) all outputs claimed by {@code tx} are in the current UTXO pool,
            if (opHashes.containsKey(i)) {
                System.out.println("in current pool: " + this.utxoPool.contains(new UTXO(opHashes.get(i), i)));
            } else {
                System.out.println("in current pool: false");
            }
        }

        System.out.println("\n\ni/o equal: " + (totalInput >= totalOutput));
        System.out.println("total input: " + (totalInput));
        System.out.println("total output: " + (totalOutput));


        System.out.println("\n\n");
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return possibleTxs;
    }

}
