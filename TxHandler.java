import java.util.*;

public class TxHandler {

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {

        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();

        // Note: all this looping isn't optimal, but it helped me separate the conditions out..
        // plus I haven't coded Java in a while so I need to take baby steps..

        /**
         * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
         */
        for (int i = 0; i < inputs.size(); i ++) {

            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);

            if(!this.utxoPool.contains(unspent)) {
               return false;
            }

        }

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
        ArrayList<UTXO> claimedUXTO = new ArrayList<UTXO>();

        for (int i = 0; i < inputs.size(); i ++) {
            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);

            for(int j =0; claimedUXTO.size() > j; j++) {
                if (unspent.equals(claimedUXTO.get(j))) {
                    return false;
                }
            }

            claimedUXTO.add(unspent);
        }

        /**
         * (4) all of {@code tx}s output values are non-negative, and
         */
        for (int i = 0; i < outputs.size(); i ++) {
            if (outputs.get(i).value <= 0) {
                return false;
            }
        }

        /**
         * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
         * values; and false otherwise.
         */
        double totalInput = 0.0;
        double totalOutput = 0.0;

        for (int i = 0; i < inputs.size(); i ++) {
            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output prevOutput = this.utxoPool.getTxOutput(unspent);
            totalInput += prevOutput.value;
        }

        for (int i = 0; i < outputs.size(); i ++) {
            totalOutput += outputs.get(i).value;
        }

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
        List<Transaction> txs = new ArrayList<>();
        List<Transaction> invalidTxs = new ArrayList<>();

        for(int j = 0 ; j < possibleTxs.length; j++){

            Transaction tx = possibleTxs[j];
            boolean validTx = isValidTx(tx);

            if (!validTx) continue;

            // remove old uxtos that are now spent..
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            for (int i=0; i<inputs.size(); i++) {
                Transaction.Input input = inputs.get(i);
                UTXO u = new UTXO(input.prevTxHash, input.outputIndex);
                this.utxoPool.removeUTXO(u);
            }

            // add new utxos to the pool
            ArrayList<Transaction.Output> outputs = tx.getOutputs();
            for (int i=0; i<outputs.size(); i++) {
                UTXO utxo = new UTXO(tx.getHash(), i);
                this.utxoPool.addUTXO(utxo, outputs.get(i));
            }

            txs.add(tx);
        }

        for (int j = 0; j < invalidTxs.size(); j++) {

            Transaction tx = invalidTxs.get(j);
            boolean validTx = isValidTx(tx);

            if (!validTx) continue;

            // remove old uxtos that are now spent..
            ArrayList<Transaction.Input> inputs = tx.getInputs();
            for (int i=0; i<inputs.size(); i++) {
                Transaction.Input input = inputs.get(i);
                UTXO u = new UTXO(input.prevTxHash, input.outputIndex);
                this.utxoPool.removeUTXO(u);
            }

            // add new utxos to the pool
            ArrayList<Transaction.Output> outputs = tx.getOutputs();
            for (int i=0; i<outputs.size(); i++) {
                UTXO utxo = new UTXO(tx.getHash(), i);
                this.utxoPool.addUTXO(utxo, outputs.get(i));
            }

            txs.add(tx);
        }

        return Arrays.copyOf(txs.toArray(), txs.size(), Transaction[].class);
    }

}