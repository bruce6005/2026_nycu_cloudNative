function OrderForm({ input, setInput, createOrder }: any) {
  return (
    <div className="form-row">
      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Input order name"
      />
      <button onClick={createOrder}>Create</button>
    </div>
  );
}

export default OrderForm;