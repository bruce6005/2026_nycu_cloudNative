function OrderList({ orders }: any) {
  return (
    <ul className="order-list">
      {orders.length === 0 ? (
        <li className="order-item">No orders yet</li>
      ) : (
        orders.map((order: any, index: number) => (
          <li key={index} className="order-item">
            <span>
              Order #{String(index + 1).padStart(3, "0")} - {order.name}
            </span>
            <span className="order-tag">Pending</span>
          </li>
        ))
      )}
    </ul>
  );
}

export default OrderList;