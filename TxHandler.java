import java.util.*;

public class TxHandler {

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);

        // All unspent transaction outputs
        ArrayList<UTXO> utxo = this.utxoPool.getAllUTXO();

    }

    public boolean isValidTx(Transaction tx) {

        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();

        /**
         * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
         */

        /**
         * (2) the signatures on each input of {@code tx} are valid,
         */
        for (int i = 0; i < inputs.size(); i ++) {

            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output prevOutput = this.utxoPool.getTxOutput(unspent);

            byte[] message = tx.getRawDataToSign(i);

            if(message == null || prevOutput == null) {
                return false;
            }

            if(!Crypto.verifySignature(prevOutput.address, message, inputs.get(i).signature)) {
                return false;
            }

        }

        /**
         * (3) no UTXO is claimed multiple times by tx
         */

        /**
         * (4) all of {@code tx}s output values are non-negative, and
         */

        /**
         * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
         * values; and false otherwise.
         */

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> transactions = new ArrayList<>();
        for(int i = 0 ; i < possibleTxs.length; i++){
            Transaction tx = possibleTxs[i];
            if(isValidTx(tx)){
                transactions.add(tx);
            }
        }
        Transaction[] validTxs = transactions.toArray(new Transaction[0]);
        return validTxs;
    }

}