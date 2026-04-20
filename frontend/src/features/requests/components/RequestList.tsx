import React from 'react';

// 定義每一筆委託單的資料格式
interface RequestData {
    id: number;
    title: string;
    status: string;
    priority: number;
    description: string;
}

interface RequestListProps {
    requests: RequestData[]; // 從外部傳入的委託單清單
    onSelect: (id: number) => void; // 新增：點擊後的動作
}

export const RequestList: React.FC<RequestListProps> = ({ requests, onSelect }) => {
    return (
        <div style={{ marginTop: '1rem' }}>
            {requests.length === 0 ? (
                <p>目前沒有任何委託單</p>
            ) : (
                <ul className="order-list">
                    {requests.map((req) => (
                        <li key={req.id} className="order-item" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', marginBottom: '4px' }}>
                                <span 
                                    style={{ fontWeight: 'bold', color: '#0066cc', cursor: 'pointer', textDecoration: 'underline' }}
                                    onClick={() => onSelect(req.id)}
                                >
                                    #{req.id} - {req.title}
                                </span>
                                <span className="order-tag" style={{ backgroundColor: req.status === 'draft' ? '#fff7e6' : '#f5f5f5', color: req.status === 'draft' ? '#fa8c16' : '#000' }}>
                                    {req.status}
                                </span>
                            </div>
                            <div style={{ fontSize: '13px', color: '#666' }}>
                                <span className="order-tag" style={{ marginRight: '8px' }}>優先度: {req.priority}</span>
                                {req.description}
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};
