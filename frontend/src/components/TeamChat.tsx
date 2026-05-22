import React, { useCallback, useEffect, useRef, useState } from 'react';
import { canModifyMessage, teamChatApi, TeamMessage } from '../services/teamChatApi';
import './TeamChat.css';

interface TeamChatProps {
  currentUserId?: string | number;
}

const TeamChat: React.FC<TeamChatProps> = ({ currentUserId }) => {
  const [messages, setMessages] = useState<TeamMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editDraft, setEditDraft] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const scrollToBottom = () => {
    const el = listRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  };

  const loadMessages = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const data = await teamChatApi.getMessages();
      setMessages(data);
      setError(null);
    } catch {
      if (!silent) {
        setError('Impossible de charger la conversation.');
      }
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMessages();
    pollRef.current = setInterval(() => loadMessages(true), 8000);
    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [loadMessages]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    const text = draft.trim();
    if (!text || sending) return;

    setSending(true);
    try {
      const saved = await teamChatApi.sendMessage(text);
      setMessages((prev) => [...prev, saved]);
      setDraft('');
      setError(null);
    } catch {
      setError("Échec de l'envoi. Réessayez.");
    } finally {
      setSending(false);
    }
  };

  const startEdit = (msg: TeamMessage) => {
    setEditingId(msg.id);
    setEditDraft(msg.content);
    setError(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditDraft('');
  };

  const saveEdit = async (messageId: number) => {
    const text = editDraft.trim();
    if (!text || actionLoading) return;

    setActionLoading(true);
    try {
      const updated = await teamChatApi.updateMessage(messageId, text);
      setMessages((prev) => prev.map((m) => (m.id === messageId ? updated : m)));
      cancelEdit();
      setError(null);
    } catch (err: unknown) {
      const msg =
        axiosErrorMessage(err) ||
        'Impossible de modifier le message (délai de 15 min dépassé ?).';
      setError(msg);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (messageId: number) => {
    if (actionLoading) return;
    if (!window.confirm('Supprimer ce message ?')) return;

    setActionLoading(true);
    try {
      await teamChatApi.deleteMessage(messageId);
      setMessages((prev) => prev.filter((m) => m.id !== messageId));
      if (editingId === messageId) cancelEdit();
      setError(null);
    } catch (err: unknown) {
      const msg =
        axiosErrorMessage(err) ||
        'Impossible de supprimer le message (délai de 15 min dépassé ?).';
      setError(msg);
    } finally {
      setActionLoading(false);
    }
  };

  const formatTime = (iso: string) => {
    try {
      return new Date(iso).toLocaleString('fr-FR', {
        day: '2-digit',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return '';
    }
  };

  const isOwnMessage = (msg: TeamMessage) =>
    currentUserId != null && String(msg.senderId) === String(currentUserId);

  return (
    <section className="team-chat" aria-label="Conversation d'équipe">
      <div className="team-chat__header">
        <h2>Conversation d'équipe</h2>
        <p>Échangez avec votre manager et vos collègues. Vous pouvez modifier ou supprimer vos messages pendant 15 minutes.</p>
      </div>

      <div className="team-chat__body">
        {loading && messages.length === 0 ? (
          <p className="team-chat__status">Chargement des messages…</p>
        ) : (
          <div className="team-chat__messages" ref={listRef}>
            {messages.length === 0 ? (
              <p className="team-chat__empty">
                Aucun message pour l'instant. Lancez la conversation !
              </p>
            ) : (
              messages.map((msg) => {
                const own = isOwnMessage(msg);
                const modifiable = own && canModifyMessage(msg, currentUserId);
                const isEditing = editingId === msg.id;

                return (
                  <div
                    key={msg.id}
                    className={`team-chat__bubble ${own ? 'team-chat__bubble--own' : 'team-chat__bubble--other'}`}
                  >
                    <div className="team-chat__meta">
                      <span className="team-chat__author">{msg.senderName}</span>
                      {msg.senderRole && (
                        <span className="team-chat__role">{msg.senderRole}</span>
                      )}
                      <span className="team-chat__time">
                        {formatTime(msg.sentAt)}
                        {msg.editedAt && ' · modifié'}
                      </span>
                    </div>

                    {isEditing ? (
                      <div className="team-chat__edit-form">
                        <input
                          type="text"
                          className="team-chat__input team-chat__input--inline"
                          value={editDraft}
                          onChange={(e) => setEditDraft(e.target.value)}
                          maxLength={4000}
                          disabled={actionLoading}
                          aria-label="Modifier le message"
                        />
                        <div className="team-chat__edit-actions">
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            onClick={() => saveEdit(msg.id)}
                            disabled={actionLoading || !editDraft.trim()}
                          >
                            Enregistrer
                          </button>
                          <button
                            type="button"
                            className="btn btn-secondary btn-sm"
                            onClick={cancelEdit}
                            disabled={actionLoading}
                          >
                            Annuler
                          </button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <p className="team-chat__text">{msg.content}</p>
                        {modifiable && (
                          <div className="team-chat__actions">
                            <button
                              type="button"
                              className="team-chat__action-btn"
                              onClick={() => startEdit(msg)}
                              disabled={actionLoading}
                            >
                              Modifier
                            </button>
                            <button
                              type="button"
                              className="team-chat__action-btn team-chat__action-btn--danger"
                              onClick={() => handleDelete(msg.id)}
                              disabled={actionLoading}
                            >
                              Supprimer
                            </button>
                          </div>
                        )}
                      </>
                    )}
                  </div>
                );
              })
            )}
          </div>
        )}

        {error && <p className="team-chat__error">{error}</p>}

        <form className="team-chat__composer" onSubmit={handleSend}>
          <input
            type="text"
            className="team-chat__input"
            placeholder="Écrire un message à l'équipe…"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            maxLength={4000}
            disabled={sending}
            aria-label="Message"
          />
          <button
            type="submit"
            className="btn btn-primary team-chat__send"
            disabled={sending || !draft.trim()}
          >
            {sending ? '…' : 'Envoyer'}
          </button>
        </form>
      </div>
    </section>
  );
};

function axiosErrorMessage(err: unknown): string | null {
  if (err && typeof err === 'object' && 'response' in err) {
    const data = (err as { response?: { data?: { error?: string } } }).response?.data;
    if (data?.error) return data.error;
  }
  return null;
}

export default TeamChat;

