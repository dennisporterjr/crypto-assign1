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
        int inputSize = inputs.size();
        int outputSize = outputs.size();


        /**
         * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
         */
        HashMap<Integer,byte[]> opHashes = new HashMap<Integer, byte[]>();

        for (int i = 0; i < inputSize; i ++) {
            opHashes.put(inputs.get(i).outputIndex, inputs.get(i).prevTxHash);
        }

        for (int i = 0; i < outputSize; i ++) {
            if (opHashes.containsKey(i)) {
                if (!this.utxoPool.contains(new UTXO(opHashes.get(i), i))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        /**
         * (2) the signatures on each input of {@code tx} are valid,
         */
        for (int i = 0; i < inputSize; i ++) {

            if(tx.getInput(inputs.get(i).outputIndex) == null) {
                return false;
            }

            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output prevOutput = this.utxoPool.getTxOutput(unspent);

            byte[] message = tx.getRawDataToSign(inputs.get(i).outputIndex);

            if(message == null) {
                return false;
            }

            if(inputs.get(i).signature == null){
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

        for (int i = 0; i < inputSize; i ++) {
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
        for (int i = 0; i < outputSize; i ++) {
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

        for (int i = 0; i < inputSize; i ++) {
            UTXO unspent = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output prevOutput = this.utxoPool.getTxOutput(unspent);
            totalInput += prevOutput.value;
        }

        for (int i = 0; i < outputSize; i ++) {
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