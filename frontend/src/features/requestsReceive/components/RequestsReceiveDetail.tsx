import type { ReceiveRequestItem } from "../model/ReceiveRequestItem";

type Props = {
  request: ReceiveRequestItem | null;
  receiving: boolean;
  onReceive: () => void;
};

function RequestsReceiveDetail({ request, receiving, onReceive }: Props) {
  return (
    <div className="card column" style={{ flex: 1, gap: "16px" }}>
      <h3>Request Detail</h3>

      {!request && <p className="text-muted">請選擇一張已核准的委託單</p>}

      {request && (
        <>
          <table className="table">
            <tbody>
              <tr>
                <th>ID</th>
                <td>{request.id}</td>
              </tr>
              <tr>
                <th>Title</th>
                <td>{request.title ?? "-"}</td>
              </tr>
              <tr>
                <th>Requester</th>
                <td>{request.requesterName ?? "-"}</td>
              </tr>
              <tr>
                <th>Sample</th>
                <td>{request.sampleName ?? "-"}</td>
              </tr>
              <tr>
                <th>Machine</th>
                <td>{request.machineName ?? "Not assigned"}</td>
              </tr>
              <tr>
                <th>Recipe</th>
                <td>{request.recipeName ?? "Not assigned"}</td>
              </tr>
              <tr>
                <th>Status</th>
                <td>
                  <span className="tag approved">{request.status}</span>
                </td>
              </tr>
              <tr>
                <th>Created At</th>
                <td>{request.createdAt ?? "-"}</td>
              </tr>
            </tbody>
          </table>

          <div className="flex" style={{ justifyContent: "flex-end" }}>
            <button
              className="button primary"
              onClick={onReceive}
              disabled={receiving}
            >
              {receiving ? "Receiving..." : "Receive"}
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default RequestsReceiveDetail;