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

        // All unspent transaction outputs
        ArrayList<UTXO> utxo = this.utxoPool.getAllUTXO();

        // loop through unspent tx outputs
/*        for (UTXO txoutput : utxo) {
            Transaction.Output output = this.utxoPool.getTxOutput(txoutput);
        }*/

    }

    /**
     * @return true if:
     * (3) no UTXO is claimed multiple times by {@code tx},
     */
    public boolean isValidTx(Transaction tx) {

        ArrayList<Transaction.Input> inputs = tx.getInputs();
        int inputSize = inputs.size();

        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        int outputSize = outputs.size();

        HashMap<Integer,byte[]> opHashes;
        opHashes = new HashMap<Integer, byte[]>();

        // total inputs and outputs
        double totalInput = 0.0;
        double totalOutput = 0.0;

        for (int i = 0; i < inputSize; i ++) {

            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output ut = this.utxoPool.getTxOutput(unspent);

            // (2) the signatures on each input of {@code tx} are valid,
            boolean validSig = Crypto.verifySignature(ut.address, tx.getRawDataToSign(inputs.get(i).outputIndex), inputs.get(i).signature);
            if (!validSig) {
                return false;
            }

            totalInput += ut.value;
            opHashes.put(inputs.get(i).outputIndex, inputs.get(i).prevTxHash);
        }

        for (int i = 0; i < outputSize; i ++) {

            // (4) all of {@code tx}s output values are non-negative, and
            if (outputs.get(i).value <= 0) {
                return false;
            }

            // (1) all outputs claimed by {@code tx} are in the current UTXO pool,
            if (opHashes.containsKey(i)) {
                if (!this.utxoPool.contains(new UTXO(opHashes.get(i), i))) {
                    return false;
                }
            } else {
                return false;
            }

            totalOutput += outputs.get(i).value;
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        // values; and false otherwise.
        if (totalInput < totalOutput) {
            return false;
        }

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