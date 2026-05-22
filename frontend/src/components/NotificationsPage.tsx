import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { notificationsApi } from '../services/api';
import { getNotificationIcon, getNotificationLabel, NotificationTypeKey } from '../utils/notificationDisplay';
import './NotificationsPage.css';
import PageHeader from './layout/PageHeader';

interface Notification {
  id: number;
  message: string;
  date: string;
  type: NotificationTypeKey | string;
  read: boolean;
  entityType?: string;
  entityId?: number;
}

const NotificationsPage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [loading, setLoading] = useState(true);

  const markAsRead = async (id: number) => {
    try {
      await notificationsApi.markNotificationAsRead(id);
      setNotifications(prev => 
        prev.map((notif: Notification) => 
          notif.id === id ? { ...notif, read: true } : notif
        )
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const unreadNotifications = notifications.filter((notif: Notification) => !notif.read);
      const readPromises = unreadNotifications
        .map((notif: Notification) => notificationsApi.markNotificationAsRead(notif.id));

      await Promise.all(readPromises);
      
      setNotifications(prev => 
        prev.map((notif: Notification) => ({ ...notif, read: true }))
      );
      setUnreadCount(0);
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
    }
  };

  const deleteNotification = async (id: number) => {
    try {
      await notificationsApi.deleteNotification(id);
      setNotifications(prev => prev.filter((notif: Notification) => notif.id !== id));
      setUnreadCount(prev => {
        const deletedNotif = notifications.find((notif: Notification) => notif.id === id);
        return deletedNotif && !deletedNotif.read ? prev - 1 : prev;
      });
    } catch (error) {
      console.error('Error deleting notification:', error);
    }
  };

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        console.log('Fetching notifications...');
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        console.log('Token exists:', !!token);
        
        const response = await notificationsApi.getNotifications();
        console.log('Notifications response:', response);
        
        if (response && Array.isArray(response)) {
          const notifs = response.map((notif: any) => ({
            id: notif.id || Date.now(),
            message: notif.message || 'Notification',
            date: new Date(notif.dateCreation || '').toLocaleDateString(),
            type: notif.type || 'SYSTEM_ALERT',
            read: notif.read || false,
            entityType: notif.entityType,
            entityId: notif.entityId
          }));
          
          setNotifications(notifs);
          setUnreadCount(notifs.filter((notif: Notification) => !notif.read).length);
        }
      } catch (error) {
        console.error('Error fetching notifications:', error);
        // If we get 401, redirect to login
        if ((error as any).response?.status === 401) {
          console.log('Authentication failed, redirecting to login');
          navigate('/login');
        }
      } finally {
        setLoading(false);
      }
    };

    if (isAuthenticated) {
      fetchNotifications();
      const interval = setInterval(fetchNotifications, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, navigate]);

  if (!isAuthenticated) {
    return (
      <div className="notifications-page">
        <div className="login-prompt">
          <h2>Please log in to view notifications</h2>
          <button onClick={() => navigate('/login')} className="login-btn">
            Go to Login
          </button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="notifications-page">
        <div className="loading">Loading notifications...</div>
      </div>
    );
  }

  return (
    <div className="notifications-page">
      <PageHeader
        title="Notifications"
        subtitle={unreadCount > 0 ? `${unreadCount} non lue(s)` : 'Vous êtes à jour'}
        actions={
          unreadCount > 0 ? (
            <button type="button" onClick={markAllAsRead} className="btn btn-secondary btn-sm">
              Tout marquer comme lu
            </button>
          ) : undefined
        }
      />

      <main className="notifications-main">
        {notifications.length === 0 ? (
          <div className="no-notifications">
            <div className="empty-state">
              <div className="empty-icon">📭</div>
              <h3>No notifications yet</h3>
              <p>You're all caught up! We'll notify you when there's something new.</p>
            </div>
          </div>
        ) : (
          <div className="notifications-list">
            {notifications.map(notification => (
              <div 
                key={notification.id} 
                className={`notification-item ${notification.read ? 'read' : 'unread'} ${notification.type}`}
              >
                <div className="notification-content">
                  <div className="notification-header">
                    <span className="notification-date">{notification.date}</span>
                    <span
                      className={`notification-type ${notification.type}`}
                      title={getNotificationLabel(notification.type)}
                    >
                      {getNotificationIcon(notification.type)}
                    </span>
                  </div>
                  <span className="notification-type-label">{getNotificationLabel(notification.type)}</span>
                  <div className="notification-message">
                    {notification.message}
                  </div>
                  <div className="notification-actions">
                    {!notification.read && (
                      <button 
                        onClick={() => markAsRead(notification.id)} 
                        className="mark-read-btn"
                      >
                        Mark as Read
                      </button>
                    )}
                    <button 
                      onClick={() => deleteNotification(notification.id)} 
                      className="delete-btn"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default NotificationsPage;
