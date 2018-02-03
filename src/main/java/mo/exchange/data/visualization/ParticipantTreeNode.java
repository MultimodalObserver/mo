package mo.exchange.data.visualization;

import mo.organization.Participant;

public class ParticipantTreeNode
{
	private Participant participant;

	public ParticipantTreeNode(Participant participant) {
		this.participant = participant;
	}

	public Participant getParticipant() {
		return participant;
	}

	@Override
	public String toString() {
		return participant.id;
	}
}