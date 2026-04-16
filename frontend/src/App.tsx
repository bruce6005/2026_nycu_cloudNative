import { useState, useEffect } from "react";
import { CONFIG } from "./config";

fetch(`${CONFIG.API_BASE}/orders`)
const API_BASE = "http://localhost:8080";

function App() {
  const [orders, setOrders] = useState([]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");

  const loadOrders = async () => {
    try {
      const res = await fetch(`${API_BASE}/orders`);
      const data = await res.json();
      setOrders(data);
    } catch (e) {
      setError("Cannot load data from backend");
    }
  };

  const createOrder = async () => {
    if (!input.trim()) {
      setError("Please enter order name");
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/orders/generate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ name: input })
      });

      const data = await res.json();
      setOrders(data);
      setInput("");
    } catch (e) {
      setError("Backend not responding or crashed");
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  return (
    <>
      {/* Topbar */}
      <div className="topbar">
        <div className="logo">LAB SYSTEM</div>
        <button className="login-btn">Login</button>
      </div>

      <div className="container">
        {/* Sidebar */}
        <div className="sidebar">
          <div className="menu-item">Dashboard</div>
          <div className="menu-item">Orders</div>
          <div className="menu-item">Create Order</div>
          <div className="menu-item">Approval</div>
          <div className="menu-item">Machine Monitor</div>
          <div className="menu-item">Analytics</div>
        </div>

        {/* Content */}
        <div className="content">

          {/* Create Order */}
          <div className="card">
            <h2>Create Order</h2>

            <div className="form-row">
              <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Input order name"
              />
              <button onClick={createOrder}>Create</button>
            </div>
          </div>

          {/* Orders */}
          <div className="card">
            <h2>Recent Orders</h2>
            <ul className="order-list">
              {orders.length === 0 ? (
                <li className="order-item">No orders yet</li>
              ) : (
                orders.map((order, index) => (
                  <li key={index} className="order-item">
                    <span>
                      Order #{String(index + 1).padStart(3, "0")} - {order}
                    </span>
                    <span className="order-tag">Pending</span>
                  </li>
                ))
              )}
            </ul>
          </div>

          {/* Machine */}
          <div className="card">
            <h2>Machine Status</h2>
            <p>Machine A - Running</p>
            <p>Machine B - Error</p>
            <p>Machine C - Idle</p>
          </div>
        </div>
      </div>

      {/* Error Modal */}
      {error && (
        <div style={modalStyle}>
          <div style={modalBox}>
            <h3>Backend Error</h3>
            <p>{error}</p>
            <button onClick={() => setError("")}>OK</button>
          </div>
        </div>
      )}
      
    </>
  );
}
import type { CSSProperties } from "react";
const modalStyle: CSSProperties = {
  display: "flex",
  position: "fixed",
  top: 0,
  left: 0,
  width: "100%",
  height: "100%",
  background: "rgba(0,0,0,0.5)",
  justifyContent: "center",
  alignItems: "center"
};

const modalBox : CSSProperties= {
  background: "white",
  padding: "20px",
  borderRadius: "8px",
  width: "300px",
  textAlign: "center"
};

export default App;