import { useState, useEffect } from "react";
import OrderList from "../components/OrderList";
import OrderForm from "../components/OrderForm";
import { authFetch } from "../../utils/apiClient";

function OrderPage() {
  const [orders, setOrders] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");

  const loadOrders = async () => {
    try {
      const res = await authFetch("/orders");
      const data = await res.json();
      setOrders(data);
    } catch {
      setError("Cannot load data from backend");
    }
  };

  const createOrder = async () => {
    if (!input.trim()) {
      setError("Please enter order name");
      return;
    }

    try {
      const res = await authFetch("/orders/generate", {
        method: "POST",
        body: JSON.stringify({ name: input })
      });

      const data = await res.json();
      setOrders(data);
      setInput("");
    } catch {
      setError("Backend not responding or crashed");
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  return (
    <div className="standard-page">
      <div className="standard-page-content">
      {error && <p style={{ color: 'red' }}>{error}</p>}
      
      <div className="card">
        <h2>Create Order</h2>
        <OrderForm input={input} setInput={setInput} createOrder={createOrder} />
      </div>

      <div className="card">
        <h2>Recent Orders</h2>
        <OrderList orders={orders} />
      </div>
      </div>
    </div>
  );
}

export default OrderPage;
