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
        <div className="card">
            <h3 className="mb-1">委託單列表</h3>
            {requests.length === 0 ? (
                <p className="text-muted">目前沒有任何委託單</p>
            ) : (
                <div className="column">
                    {requests.map((req) => (
                        <div 
                            key={req.id} 
                            className="list-item column" 
                            style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px', marginBottom: '8px' }}
                            onClick={() => onSelect(req.id)}
                        >
                            <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                                <strong style={{ fontSize: '15px' }}>#{req.id} - {req.title}</strong>
                                <span className={`tag ${req.status.toLowerCase()}`}>
                                    {req.status}
                                </span>
                            </div>
                            <div className="text-muted mt-1" style={{ fontSize: '13px' }}>
                                <span className="tag secondary" style={{ marginRight: '8px', background: '#eee' }}>
                                    優先度: {req.priority}
                                </span>
                                {req.description && req.description.length > 50 
                                    ? req.description.substring(0, 50) + '...' 
                                    : req.description}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
